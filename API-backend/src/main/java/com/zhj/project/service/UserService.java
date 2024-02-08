package com.zhj.project.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.zhj.common.model.dto.user.*;
import com.zhj.common.model.entity.User;
import com.zhj.common.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务
 *
 * @author zhj
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @return 新用户 id
     */
    long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    UserVO getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是游客
     *
     * @param request 要求
     * @return {@link User}
     */
    User isTourist(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    boolean updateKey(Long id);


    /**
     * 添加钱包余额
     *
     * @param userId    用户id
     * @param addPoints 添加点
     * @return boolean
     */
    boolean addWalletBalance(Long userId, Integer addPoints);

    User wxAuth(String code);

    UserVO userBindEmail(UserBindEmailRequest userBindEmailRequest, HttpServletRequest request);

    long userEmailRegister(UserEmailRegisterRequest userEmailRegisterRequest);

    UserVO userUnBindEmail(UserUnBindEmailRequest userUnBindEmailRequest, HttpServletRequest request);

    UserVO userEmailLogin(UserEmailLoginRequest userEmailLoginRequest, HttpServletRequest request);
}
