package com.zhj.project.service.impl;

import com.zhj.project.service.RequestMethod;

/**
 * @author zhj
 * @version 1.0
 * @date 2023/8/7 15:54
 */
public class RequestMethodFactory {

    public RequestMethod getBean(String type) {
        RequestMethod requestMethod = null;
        if ("POST".equals(type)) {
            requestMethod = new PostRequestMethod();
        } else if ("GET".equals(type)) {
            requestMethod = new GetRequestMethod();
        } else {
            requestMethod = new GetRequestMethod();
        }
        return requestMethod;
    }
}
