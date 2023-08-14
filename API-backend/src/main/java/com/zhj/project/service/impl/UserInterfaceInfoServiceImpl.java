package com.zhj.project.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.zhj.common.constant.CaffeineConstant;
import com.zhj.common.model.entity.InterfaceInfo;
import com.zhj.common.model.entity.UserInterfaceInfo;
import com.zhj.common.model.vo.InterfaceInfoVO;
import com.zhj.common.utils.ErrorCode;
import com.zhj.common.constant.RedisConstant;
import com.zhj.project.exception.BusinessException;
import com.zhj.project.mapper.UserInterfaceInfoMapper;
import com.zhj.project.service.InterfaceInfoService;
import com.zhj.project.service.UserInterfaceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.zhj.common.constant.CommonConstant.INTERFACE_RANK;
import static com.zhj.common.constant.MqConstant.EXCHANGE_DIRECT_INTERFACE;
import static com.zhj.common.constant.MqConstant.ROUTERING_KEY_INTERFACE;


/**
 * @author 鏈辩剷鏉�
 * @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service实现
 * @createDate 2023-04-06 15:13:13
 */
@Service
@Slf4j
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
        implements UserInterfaceInfoService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private Cache<String, UserInterfaceInfo> userInterfaceInfoCache;

    @Resource
    private Cache<String, Integer> integerCache;

    private static final DefaultRedisScript<Long> REDUCE_SCRIPT;

    static {
        REDUCE_SCRIPT = new DefaultRedisScript<>();
        REDUCE_SCRIPT.setResultType(Long.class);
        REDUCE_SCRIPT.setLocation(new ClassPathResource("reduce.lua"));
    }

    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //创建时
        if (add) {
            if (userInterfaceInfo.getInterfaceInfoId() <= 0 || userInterfaceInfo.getUserId() <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口或用户不存在");
            }
        }
        if (userInterfaceInfo.getLeftNum() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余次数不能小于0");
        }
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


    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        //调用lua脚本
        Long result = null;
        try {
            result = stringRedisTemplate.execute(
                    REDUCE_SCRIPT,
                    Collections.singletonList(RedisConstant.INTERFACE_LEFTCOUNT),
                    userId + "", interfaceInfoId + ""
            );
        } catch (Exception e) {
            return false;
        }
        //判断结果 1:剩余次数不足 0:剩余次数充足
        if (result.intValue() == 1) {
            return false;
        }
        //发送mq更新数据库，让用户对应的接口剩余次数-1，总调用次数+1；更新redis的调用次数排行榜
        changeInterfaceInfoCount(userId, interfaceInfoId);
        return true;
    }

    @Override
    @Transactional
    public void changeCount(Long userId, Long interfaceInfoId) {
        //更新接口调用次数
        baseMapper.changeCount(userId, interfaceInfoId);
    }

    //更新排行榜
    @Override
    public void updateInterfaceRankList(Long userId, Long interfaceInfoId) {
        InterfaceInfo interfaceInfo = interfaceInfoService.getInterfaceById(interfaceInfoId);
        InterfaceInfoVO interfaceInfoVO = new InterfaceInfoVO();
        BeanUtils.copyProperties(interfaceInfo, interfaceInfoVO);
        interfaceInfoVO.setUpdateTime(null);
        stringRedisTemplate.opsForZSet().incrementScore(INTERFACE_RANK, JSON.toJSONString(interfaceInfoVO), 1);
    }

    /**
     * 是否还有剩余调用次数
     *
     * @param id
     * @param userId
     * @return
     */
    @Override
    public boolean isNumOfUse(Long id, Long userId) {
        String numStr = null;
        try {
            numStr = stringRedisTemplate.opsForValue().get(RedisConstant.INTERFACE_LEFTCOUNT + userId + ":" + id);
            return Long.parseLong(numStr) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public boolean saveUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo) {
        boolean flag = save(userInterfaceInfo);
        if (flag) {
            String key = RedisConstant.INTERFACE_LEFTCOUNT
                    + userInterfaceInfo.getUserId() + ":" + userInterfaceInfo.getInterfaceInfoId();
            //将剩余次数存入reids
            stringRedisTemplate.opsForValue().set(key, userInterfaceInfo.getLeftNum().toString(), RedisConstant.EXPIRE_TIME, TimeUnit.MINUTES);
            //剩余次数
            integerCache.put(key, userInterfaceInfo.getLeftNum());
            String key1 = CaffeineConstant.USER_INTERFACE_INFO + +userInterfaceInfo.getId();
            //详细信息
            userInterfaceInfoCache.put(key1, userInterfaceInfo);
        }
        return flag;
    }

    @Override
    @Transactional
    public boolean updateUserInterfaceById(UserInterfaceInfo userInterfaceInfo) {
        boolean flag = updateById(userInterfaceInfo);
        if (flag) {
            String key = RedisConstant.INTERFACE_LEFTCOUNT
                    + userInterfaceInfo.getUserId() + ":" + userInterfaceInfo.getInterfaceInfoId();
            stringRedisTemplate.delete(key);
            String key1 = CaffeineConstant.USER_INTERFACE_INFO + userInterfaceInfo.getId();
            integerCache.invalidate(key);
            userInterfaceInfoCache.invalidate(key1);
        }
        return flag;
    }

    @Override
    @Transactional
    public boolean removeUserInterfaceById(UserInterfaceInfo userInterfaceInfo) {
        boolean flag = removeById(userInterfaceInfo.getId());
        if (flag) {
            String key = RedisConstant.INTERFACE_LEFTCOUNT
                    + userInterfaceInfo.getUserId() + ":" + userInterfaceInfo.getInterfaceInfoId();
            stringRedisTemplate.delete(key);
            String key1 = CaffeineConstant.USER_INTERFACE_INFO + userInterfaceInfo.getId();
            integerCache.invalidate(key);
            userInterfaceInfoCache.invalidate(key1);
        }
        return flag;
    }

    @Override
    public UserInterfaceInfo getUserInterfaceById(long id) {
        String key1 = CaffeineConstant.USER_INTERFACE_INFO + id;
        return userInterfaceInfoCache.get(key1, item -> getById(id));
    }

    //@Override
    //@Transactional
    //public boolean invokeCount(long interfaceInfoId, long userId) {
    //    //判断
    //    if (interfaceInfoId <= 0 || userId <= 0) {
    //        throw new BusinessException(ErrorCode.PARAMS_ERROR);
    //    }
    //    LambdaUpdateWrapper<UserInterfaceInfo> updateWrapper = new LambdaUpdateWrapper<>();
    //    updateWrapper.eq(UserInterfaceInfo::getInterfaceInfoId, interfaceInfoId);
    //    updateWrapper.eq(UserInterfaceInfo::getUserId, userId);
    //    updateWrapper.gt(UserInterfaceInfo::getLeftNum, 0);
    //    updateWrapper.setSql("leftNum = leftNum -1 , totalNum = totalNum + 1");
    //    return update(updateWrapper);
    //}
}
