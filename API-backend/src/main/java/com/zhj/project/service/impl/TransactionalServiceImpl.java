package com.zhj.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhj.common.constant.UserConstant;
import com.zhj.common.model.entity.User;
import com.zhj.project.mapper.UserMapper;
import com.zhj.project.service.TransactionalService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.Map;

@Service
@Transactional
public class TransactionalServiceImpl implements TransactionalService {

    @Resource
    private UserMapper userMapper;

    @Override
    public User addUser(Map<String, String> userInfo) {
        String unionid = userInfo.get("unionid");
        User user = userMapper.selectOne(new QueryWrapper<User>().eq(StringUtils.isNotBlank(unionid), "user_account", unionid));
        if (user != null) {
            return user;
        }
        // 创建用户
        user = new User();
        // 账号
        user.setUserAccount(unionid);
        // 密码
        String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + "123456789").getBytes());
        user.setUserPassword(encryptPassword);
        // 昵称
        String nickname = userInfo.get("nickname");
        user.setUserName(nickname);
        // 性别(1男2女)
        String sex = userInfo.get("sex");
        //user.setGender(Integer.valueOf(sex));
        // 头像
        String img = userInfo.get("headimgurl");
        user.setUserAvatar(img);
        userMapper.insert(user);
        return user;
    }
}