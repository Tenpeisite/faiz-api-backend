package com.zhj.project.service.impl;

import com.alibaba.fastjson.JSON;
import com.zhj.apiclientsdk.client.ApiClient;
import com.zhj.project.service.RequestMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author zhj
 * @version 1.0
 * @date 2023/8/7 15:47
 */
public class PostRequestMethod implements RequestMethod {


    @Override
    public Object invoke(ApiClient client, String methodName, String requestParams) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<? extends ApiClient> clazz = client.getClass();
        //将参数转成Map
        Map map = JSON.parseObject(requestParams, Map.class);
        //反射调用方法
        Method method = clazz.getMethod(methodName, Map.class);
        return method.invoke(client, map);
    }
}
