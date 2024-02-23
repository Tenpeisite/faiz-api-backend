package com.zhj.project.service.impl.inner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.zhj.common.constant.RedisConstant;
import com.zhj.common.model.entity.User;
import com.zhj.common.utils.ErrorCode;
import com.zhj.project.exception.BusinessException;
import com.zhj.project.service.UserService;
import com.zhj.common.service.InnerUserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author 朱焕杰
 * @version 1.0
 * @description TODO
 * @date 2023/4/10 22:02
 */
@DubboService
public class InnerUserServiceImpl implements InnerUserService {

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public User getInvokeUser(String accessKey) {
        if (StringUtils.isAnyBlank(accessKey)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccessKey, accessKey);
        User user = userService.getOne(queryWrapper);
        return user;
    }

    @Override
    public boolean isBalance(Long userId) {
        String numStr = null;
        try {
            numStr = stringRedisTemplate.opsForValue().get(RedisConstant.INTERFACE_LEFTCOUNT + userId);
            return Long.parseLong(numStr) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
