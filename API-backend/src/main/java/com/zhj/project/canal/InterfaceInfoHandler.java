package com.zhj.project.canal;

import com.github.benmanes.caffeine.cache.Cache;

import com.zhj.common.constant.RedisConstant;
import com.zhj.common.model.entity.UserInterfaceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

/**
 * @author 朱焕杰
 * @version 1.0
 * @description TODO
 * @date 2023/4/25 15:43
 */
@CanalTable("user_interface_info")
@Component
public class InterfaceInfoHandler implements EntryHandler<UserInterfaceInfo> {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private Cache<String, Integer> intCache;

    /**
     * 新增时同步缓存(用户的剩余调用次数)
     * @param interfaceInfo
     */
    @Override
    public void insert(UserInterfaceInfo interfaceInfo) {
        String key = RedisConstant.INTERFACE_LEFTCOUNT
                + interfaceInfo.getUserId() + ":" + interfaceInfo.getInterfaceInfoId();
        //写入jvm进程缓存
        intCache.put(key, interfaceInfo.getLeftNum());
        //写入redis缓存
        stringRedisTemplate.opsForValue().set(key, interfaceInfo.getLeftNum().toString());
    }

    /**
     * 修改时同步缓存(用户的剩余调用次数)
     * @param before
     * @param after
     */
    @Override
    public void update(UserInterfaceInfo before, UserInterfaceInfo after) {
        String key = RedisConstant.INTERFACE_LEFTCOUNT
                + after.getUserId() + ":" + after.getInterfaceInfoId();
        //写入jvm进程缓存
        intCache.put(key, after.getLeftNum());
        //写入redis缓存
        stringRedisTemplate.opsForValue().set(key, after.getLeftNum().toString());
    }

    /**
     * 删除时，删除jvm缓存和redis缓存
     * @param interfaceInfo
     */
    @Override
    public void delete(UserInterfaceInfo interfaceInfo) {
        String key = RedisConstant.INTERFACE_LEFTCOUNT
                + interfaceInfo.getUserId() + ":" + interfaceInfo.getInterfaceInfoId();
        //删除jvm进程缓存
        intCache.invalidate(key);
        //删除redis缓存
        stringRedisTemplate.delete(key);
    }
}
