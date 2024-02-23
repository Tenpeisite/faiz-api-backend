package com.zhj.project.service;

import com.zhj.apiclientsdk.client.ApiClient;
import com.zhj.common.model.dto.interfaceinfo.InvokeRequest;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * @author zhj
 * @version 1.0
 * @date 2023/8/7 15:44
 */
public interface RequestMethod {

    public Object invoke(ApiClient client, String methodName, List<InvokeRequest.Field> requestParams) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;

}
