package com.zhj.common.service;


import com.zhj.common.model.entity.User;

/**
 * 用户服务
 *
 * @author zhj
 */
public interface InnerUserService{
    /**
     * 查询数据库中该密钥对应的用户
     *
     * @param accessKey
     * @param secretKey
     * @return
     */
    User getInvokeUser(String accessKey);

    /**
     * 是否还有余额
     * @param userId
     * @return
     */
    boolean isBalance(Long userId);
}
