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
 * @date 2023/4/28 11:45
 */
@RestController
@Slf4j
public class BoTianController {

    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;

    @GetMapping("/dujitang")
    public String getDuJiTang(HttpServletRequest request) {
        String path = request.getRequestURI();
        //查询接口的url
        String url = innerInterfaceInfoService.getInterfaceUrl(path);
        HttpResponse response = HttpRequest.get(url).execute();
        log.info("毒鸡汤：{}", response.body());
        return response.body();
    }
}
