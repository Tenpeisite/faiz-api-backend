package com.zhj.apiinterface.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.zhj.common.service.InnerInterfaceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 朱焕杰
 * @version 1.0
 * @description TODO
 * @date 2023/4/27 18:04
 */
@RestController
@Slf4j
public class XiaRouController {

    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;

    @GetMapping("/yiyan")
    public String getYiYan(HttpServletRequest request) {
        String path = request.getRequestURI();
        //查询接口的url
        String url = innerInterfaceInfoService.getInterfaceUrl(path);
        HttpResponse response = HttpRequest.get(url).execute();
        log.info("每日一言：{}", response.body());
        return response.body();
    }

    @GetMapping("/dygirl")
    public String getdyGirlVideo(HttpServletRequest request) {
        String path = request.getRequestURI();
        //查询接口的url
        String url = innerInterfaceInfoService.getInterfaceUrl(path);
        HttpResponse response = HttpRequest.get(url).execute();
        log.info("抖音美女视频：{}", response.body());
        return response.body();
    }

    @GetMapping("/qqimage")
    public String getQQimage(HttpServletRequest request, String qq) {
        String path = request.getRequestURI();
        //查询接口的url
        String url = innerInterfaceInfoService.getInterfaceUrl(path);
        HttpResponse response = HttpRequest.get(url+qq).execute();
        log.info("qq头像：{}", response.body());
        return response.body();
    }
}
