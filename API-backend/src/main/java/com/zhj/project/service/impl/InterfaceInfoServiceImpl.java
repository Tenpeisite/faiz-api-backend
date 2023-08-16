package com.zhj.project.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.zhj.common.constant.CaffeineConstant;
import com.zhj.common.constant.RedisConstant;
import com.zhj.common.model.entity.InterfaceInfo;
import com.zhj.common.utils.ErrorCode;
import com.zhj.project.exception.BusinessException;
import com.zhj.project.mapper.InterfaceInfoMapper;
import com.zhj.project.service.InterfaceInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springfox.documentation.spring.web.json.Json;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author 鏈辩剷鏉�
 * @description 针对表【interface_info(接口信息)】的数据库操作Service实现
 * @createDate 2023-04-03 22:17:53
 */
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo> implements InterfaceInfoService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private Cache<String, InterfaceInfo> interfaceInfoCache;

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
}
