package com.zhj.project.service;

import com.zhj.apiclientsdk.client.ApiClient;

import java.lang.reflect.InvocationTargetException;

/**
 * @author zhj
 * @version 1.0
 * @date 2023/8/7 15:44
 */
public interface RequestMethod {

    public Object invoke(ApiClient client, String methodName, String requestParams) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;

}
