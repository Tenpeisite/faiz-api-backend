package com.zhj.common.service;


import com.zhj.common.model.entity.InterfaceInfo;

public interface InnerInterfaceInfoService{

    /**
     * 从数据库中查询模拟接口是否存在（请求路径，请求方法，请求参数）
     * @param path
     * @param method
     * @return
     */
    InterfaceInfo getInterfoceInfo(String methodName, String method);

    String getInterfaceUrl(String methodName);

    boolean invokeCount(Long interfaceId,Long userId);
}