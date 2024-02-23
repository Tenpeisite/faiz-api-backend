package com.zhj.apiinterface.controller;



import cn.hutool.json.JSONUtil;
import com.zhj.common.model.entity.User;
import com.zhj.common.service.InnerUsernameService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author 朱焕杰
 * @version 1.0
 * @description TODO
 * @date 2023/4/4 16:19
 */
@RestController
@RequestMapping("/name")
public class NameController {


    @GetMapping("/{json}")
    public String getNameByGet(@PathVariable("json") String json) {
        Map map = JSONUtil.toBean(json, Map.class);
        return "GET 你的名字是" + map.get("name");
    }

    @PostMapping("/post")
    public String getNameByPost(@RequestParam String name) {
        return "POST 你的名字是" + name;
    }

    @PostMapping("/user")
    public String getUserNameByPost(@RequestBody User user, HttpServletRequest request) {
        //String accessKey = request.getHeader("accessKey");
        //String nonce = request.getHeader("nonce");
        //String timestamp = request.getHeader("timestamp");
        //String sign = request.getHeader("sign");
        //String body = request.getHeader("body");
        //if (!accessKey.equals("zhj")) {
        //    throw new RuntimeException("无权限");
        //}
        //if (Long.parseLong(nonce) > 10000) {
        //    throw new RuntimeException("无权限");
        //}
        ////时间和当前时间不能超过5min
        //if (System.currentTimeMillis() / 1000 - Long.parseLong(timestamp) > 5 * 60) {
        //    throw new RuntimeException("无权限");
        //}
        //String serverSign = SignUtils.getSign(body, "abcd");
        //if (!sign.equals(serverSign)) {
        //    throw new RuntimeException("无权限");
        //}
        return "POST 用户名字是" + user.getUserName();
    }

}
