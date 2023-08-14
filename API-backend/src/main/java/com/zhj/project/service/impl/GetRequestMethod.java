package com.zhj.project.service.impl;


import com.zhj.apiclientsdk.client.ApiClient;
import com.zhj.project.service.RequestMethod;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author zhj
 * @version 1.0
 * @date 2023/8/7 15:57
 */
public class GetRequestMethod implements RequestMethod {

    @Override
    public Object invoke(ApiClient client, String methodName, String requestParams) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = null;
        Class<? extends ApiClient> clazz = client.getClass();
        if (StringUtils.isBlank(requestParams)) {
            method = clazz.getMethod(methodName);
            return method.invoke(client);
        } else {
            method = clazz.getMethod(methodName, String.class);
            return method.invoke(client, requestParams);
        }
    }
}
