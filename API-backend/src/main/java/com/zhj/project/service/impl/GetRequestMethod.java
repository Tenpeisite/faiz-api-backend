package com.zhj.project.service.impl;


import com.zhj.apiclientsdk.client.ApiClient;
import com.zhj.common.model.dto.interfaceinfo.InvokeRequest;
import com.zhj.project.service.RequestMethod;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author zhj
 * @version 1.0
 * @date 2023/8/7 15:57
 */
public class GetRequestMethod implements RequestMethod {

    @Override
    public Object invoke(ApiClient client, String methodName, List<InvokeRequest.Field> requestParams) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = null;
        Class<? extends ApiClient> clazz = client.getClass();
        if (requestParams == null) {
            method = clazz.getMethod(methodName);
            return method.invoke(client);
        } else {
            Class[] classes = new Class[requestParams.size()];
            Object[] args = new Object[requestParams.size()];
            for (int i = 0; i < requestParams.size(); i++) {
                args[i] = requestParams.get(i).getValue();
                classes[i] = requestParams.get(i).getValue().getClass();
            }
            method = clazz.getMethod(methodName, classes);
            return method.invoke(client, args);
        }
    }
}
