package com.zhj.project.runner;

import com.alibaba.fastjson.JSON;

import com.zhj.common.constant.RedisConstant;
import com.zhj.common.model.entity.InterfaceInfo;
import com.zhj.common.model.entity.User;
import com.zhj.common.model.entity.UserInterfaceInfo;
import com.zhj.common.model.vo.InterfaceInfoVO;
import com.zhj.project.mapper.UserInterfaceInfoMapper;
import com.zhj.project.service.InterfaceInfoService;
import com.zhj.project.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.zhj.common.constant.CommonConstant.INTERFACE_RANK;

/**
 * @author 朱焕杰
 * @version 1.0
 * @description TODO
 * @date 2023/4/22 14:04
 */
@Component
public class InitTask implements CommandLineRunner {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Autowired
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        //加载所有接口的总调用次数到redis
        //loadAllInterfaceInvokeCount2Redis();
        //加载每个用户的接口剩余调用次数到redis
        //loadAllLeftCount2Redis();

        //加载每个用户的余额到redis
        loadAllUserBalance2Redis();
    }

    /**
     * //加载所有接口的总调用次数到redis
     */
    private void loadAllInterfaceInvokeCount2Redis() {
        //删除数据，重新加载
        stringRedisTemplate.delete(INTERFACE_RANK);
        //查询每个接口的调用次数
        List<UserInterfaceInfo> userInterfaceInfos = userInterfaceInfoMapper.listAllInterfaceInfo();
        //将数据加载到redis
        userInterfaceInfos.forEach(item -> {
            InterfaceInfo interfaceInfo = interfaceInfoService.getById(item.getInterfaceInfoId());
            InterfaceInfoVO interfaceInfoVO = new InterfaceInfoVO();
            BeanUtils.copyProperties(interfaceInfo, interfaceInfoVO);
            interfaceInfoVO.setUpdateTime(null);
            //interfaceInfoVO.setTotalNum(item.getTotalNum());
            stringRedisTemplate.opsForZSet().add(INTERFACE_RANK, JSON.toJSONString(interfaceInfoVO), item.getTotalNum());
        });
    }

    /**
     * 加载每个用户的接口剩余调用次数到redis
     */
    private void loadAllLeftCount2Redis() {
        List<UserInterfaceInfo> userInterfaceInfos = userInterfaceInfoMapper.listAllLeftCount();
        userInterfaceInfos.forEach(interfaceInfo -> {
            String key = RedisConstant.INTERFACE_LEFTCOUNT
                    + interfaceInfo.getUserId() + ":" + interfaceInfo.getInterfaceInfoId();
            //将剩余次数存入reids
            stringRedisTemplate.opsForValue().set(key, interfaceInfo.getLeftNum().toString());
        });
    }

    private void loadAllUserBalance2Redis() {
        List<User> users = userService.list();
        for (User user : users) {
            stringRedisTemplate.opsForValue().set(RedisConstant.INTERFACE_LEFTCOUNT + user.getId(), user.getBalance().toString());
        }
    }
}
