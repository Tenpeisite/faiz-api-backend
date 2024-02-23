package com.zhj.apiinterface.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.zhj.common.service.InnerInterfaceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

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
        String url = get(request);
        HttpResponse response = HttpRequest.get(url).execute();
        log.info("毒鸡汤：{}", response.body());
        return response.body();
    }

    @GetMapping("/bizi")
    public Map getBizi(HttpServletRequest request) {
        String url = get(request);
        HttpResponse response = HttpRequest.get(url).execute();
        Map map = JSONUtil.toBean(response.body(), Map.class);
        log.info("壁纸：{}", map.get("imgurl"));
        return map;
    }

    @GetMapping("/horoscope")
    public Map getHoroscope(String type, String time, HttpServletRequest request) {
        String url = get(request);
        url = url + "?type=" + type + "&time=" + time;
        HttpResponse response = HttpRequest.get(url).execute();
        Map map = JSONUtil.toBean(response.body(), Map.class);
        return map;
    }

    @GetMapping("/loveTalk")
    public String getLoveTalk(HttpServletRequest request) {
        String url = get(request);
        HttpResponse response = HttpRequest.get(url).execute();
        log.info("毒鸡汤：{}", response.body());
        return response.body();
    }

    @GetMapping("/ipLocation")
    public Map getIpLocation(String ip, HttpServletRequest request) {
        String url = get(request);
       if(StringUtils.isNotBlank(ip)){
           url = url + "?ip=" + ip;
       }
        HttpResponse response = HttpRequest.get(url).execute();
        Map map = JSONUtil.toBean(response.body(), Map.class);
        return map;
    }

    @GetMapping("/weather")
    public Map getIpLocation(String city, String ip, String type, HttpServletRequest request) {
        String url = get(request);
        if(StringUtils.isNoneBlank(city,ip,type)){
        url = url + "?city=" + city + "&ip=" + ip + "&type=" + type;
        }
        HttpResponse response = HttpRequest.get(url).execute();
        Map map = JSONUtil.toBean(response.body(), Map.class);
        return map;
    }


    private String get(HttpServletRequest request) {
        String methodName = request.getHeader("methodName");
        //查询接口的url
        String url = innerInterfaceInfoService.getInterfaceUrl(methodName);
        return url;
    }
}
