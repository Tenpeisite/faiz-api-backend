package com.zhj.project.service;

import com.zhj.common.model.entity.User;

import java.util.Map;

/**
 * @author zhj
 * @version 1.0
 * @date 2023/8/7 18:35
 */
public interface TransactionalService {
    public User addUser(Map<String, String> userInfo);
}
