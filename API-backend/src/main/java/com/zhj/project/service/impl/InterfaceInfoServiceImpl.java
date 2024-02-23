package com.zhj.project.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.zhj.common.constant.CaffeineConstant;
import com.zhj.common.constant.RedisConstant;
import com.zhj.common.model.entity.InterfaceInfo;
import com.zhj.common.model.entity.User;
import com.zhj.common.utils.ErrorCode;
import com.zhj.project.exception.BusinessException;
import com.zhj.project.mapper.InterfaceInfoMapper;
import com.zhj.project.service.InterfaceInfoService;
import com.zhj.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springfox.documentation.spring.web.json.Json;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.zhj.common.constant.MqConstant.EXCHANGE_DIRECT_INTERFACE;
import static com.zhj.common.constant.MqConstant.ROUTERING_KEY_INTERFACE;

/**
 * @author 鏈辩剷鏉�
 * @description 针对表【interface_info(接口信息)】的数据库操作Service实现
 * @createDate 2023-04-03 22:17:53
 */
@Service
@Slf4j
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo> implements InterfaceInfoService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private Cache<String, InterfaceInfo> interfaceInfoCache;

    @Resource
    private UserService userService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    private static final DefaultRedisScript<Long> REDUCE_SCRIPT;

    static {
        REDUCE_SCRIPT = new DefaultRedisScript<>();
        REDUCE_SCRIPT.setResultType(Long.class);
        REDUCE_SCRIPT.setLocation(new ClassPathResource("reduce.lua"));
    }

    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = interfaceInfo.getName();
        //创建时
        if (add) {
            if (StringUtils.isAnyBlank(name)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
        if (StringUtils.isNotBlank(name) && name.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
    }

    @Override
    @Transactional
    public boolean saveInterface(InterfaceInfo interfaceInfo) {
        boolean flag = save(interfaceInfo);
        if (flag) {
            String key = RedisConstant.INTERFACE + interfaceInfo.getId();
            stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(interfaceInfo), RedisConstant.EXPIRE_TIME, TimeUnit.MINUTES);
            String key1 = CaffeineConstant.INTERFACE + interfaceInfo.getId();
            interfaceInfoCache.put(key1, interfaceInfo);
        }
        return flag;
    }

    @Override
    @Transactional
    public boolean removeInterfaceById(long id) {
        boolean flag = removeById(id);
        if (flag) {
            String key = RedisConstant.INTERFACE + id;
            stringRedisTemplate.delete(key);
            String key1 = CaffeineConstant.INTERFACE + id;
            interfaceInfoCache.invalidate(key1);
        }
        return flag;
    }

    @Override
    public boolean updateInterfaceById(InterfaceInfo interfaceInfo) {
        boolean flag = updateById(interfaceInfo);
        if (flag) {
            String key = RedisConstant.INTERFACE + interfaceInfo.getId();
            stringRedisTemplate.delete(key);
            String key1 = CaffeineConstant.INTERFACE + interfaceInfo.getId();
            interfaceInfoCache.invalidate(key1);
        }
        return flag;
    }

    @Override
    public InterfaceInfo getInterfaceById(long id) {
        String key = RedisConstant.INTERFACE + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(json)) {
            return JSON.parseObject(json, InterfaceInfo.class);
        }
        String key1 = CaffeineConstant.INTERFACE + id;
        InterfaceInfo interfaceInfo = interfaceInfoCache.get(key1, item -> getById(id));
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(interfaceInfo), RedisConstant.EXPIRE_TIME, TimeUnit.MINUTES);
        return interfaceInfo;
    }

    @Override
    public boolean invokeCount(Long interfaceId, Long userId) {
        //调用lua脚本
        Long result = null;
        try {
            result = stringRedisTemplate.execute(
                    REDUCE_SCRIPT,
                    Collections.singletonList(RedisConstant.INTERFACE_LEFTCOUNT),
                    userId + ""
            );
        } catch (Exception e) {
            return false;
        }
        //判断结果 1:剩余次数不足 0:剩余次数充足
        if (result.intValue() == 1) {
            return false;
        }
        //发送mq更新数据库，让用户余额-1，接口调用次数+1
        changeInterfaceInfoCount(userId, interfaceId);
        return true;
    }

    @Override
    public void changeCount(Long userId, Long interfaceInfoId) {
        InterfaceInfo interfaceInfo = getById(interfaceInfoId);
        //每次调用所需积分数
        Integer reduceScore = interfaceInfo.getReduceScore();
        User user = userService.getById(userId);
        user.setBalance(user.getBalance() - reduceScore);
        //修改用户余额
        userService.updateById(user);
        //修改接口调用次数
        interfaceInfo.setTotalInvokes(interfaceInfo.getTotalInvokes() + 1);
        updateById(interfaceInfo);
    }


    /**
     * 发送mq更新数据库，让用户对应的接口剩余次数-1，总调用次数+1
     *
     * @param userId
     * @param interfaceInfoId
     */
    private void changeInterfaceInfoCount(long userId, long interfaceInfoId) {
        //1.封装消息
        Map<String, Long> map = new HashMap<>();
        map.put("userId", userId);
        map.put("interfaceInfoId", interfaceInfoId);
        //2.全局唯一消息id，需要封装到 CorrelationData 中
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        //3.添加callback
        correlationData.getFuture().addCallback(
                result -> {
                    //判断是否到达交换机
                    if (result.isAck()) {
                        //3.1 ack，消息成功到达交换机
                        log.info("更新接口调用次数消息发送成功, ID:{}", correlationData.getId());
                    } else {
                        //3.2 nack，消息没有到达交换机
                        log.error("更新接口调用次数消息发送失败, ID:{}, 原因{}", correlationData.getId(), result.getReason());
                        //3.3 重发消息
                        changeInterfaceInfoCount(userId, interfaceInfoId);
                    }
                }, ex -> {
                    log.error("更新接口调用次数消息发送异常, ID:{}, 原因{}", correlationData.getId(), ex.getMessage());
                    //重发消息
                    changeInterfaceInfoCount(userId, interfaceInfoId);
                }
        );
        //发送消息
        rabbitTemplate.convertAndSend(EXCHANGE_DIRECT_INTERFACE, ROUTERING_KEY_INTERFACE, map);
    }
}
