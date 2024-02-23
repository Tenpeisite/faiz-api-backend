package com.zhj.project.service.impl;

import com.alibaba.fastjson.JSON;
import com.zhj.apiclientsdk.client.ApiClient;
import com.zhj.common.model.dto.interfaceinfo.InvokeRequest;
import com.zhj.project.service.RequestMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhj
 * @version 1.0
 * @date 2023/8/7 15:47
 */
public class PostRequestMethod implements RequestMethod {


    @Override
    public Object invoke(ApiClient client, String methodName, List<InvokeRequest.Field> requestParams) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Map map = requestParams.stream()
                .collect(Collectors.toMap(InvokeRequest.Field::getFieldName, InvokeRequest.Field::getValue));
        Class<? extends ApiClient> clazz = client.getClass();
        //反射调用方法
        Method method = clazz.getMethod(methodName, Map.class);
        return method.invoke(client, map);
    }
}
