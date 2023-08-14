package com.zhj.apiclientsdk.client;


import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.zhj.apiclientsdk.model.User;
import com.zhj.apiclientsdk.utis.SignUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 朱焕杰
 * @version 1.0
 * @description TODO
 * @date 2023/4/4 16:37
 */
@AllArgsConstructor
@NoArgsConstructor
public class ApiClient {
    private static final String GATEWAY_HOST = "http://localhost:8090";
    //private static final String GATEWAY_HOST = "http://api-gateway.tempeisite.xyz";
    private String accessKey;
    private String secretKey;


    public String getNameByGet(String name) {
        //可以单独传入http参数，这样参数会自动做URL编码，拼接在URL中
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);
        String result = HttpUtil.get(GATEWAY_HOST + "/api/name/", paramMap);
        System.out.println(result);
        return result;
    }

    public String getNameByPost(String name) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);

        String result = HttpUtil.post(GATEWAY_HOST + "/api/name/", paramMap);
        System.out.println(result);
        return result;
    }

    public String getUserNameByPost(Map map) {
        User user = new User();
        user.setUserName(map.get("name").toString());
        String json = JSONUtil.toJsonStr(user);
        HttpResponse response = HttpRequest.post(GATEWAY_HOST + "/api/name/user")
                .body(json)
                .addHeaders(getHeaderMap(json))
                .execute();
        String result = response.body();
        System.out.println(result);
        return result;
    }

    public String getRandomName() {
        //可以单独传入http参数，这样参数会自动做URL编码，拼接在URL中
        HttpResponse response = HttpRequest.get(GATEWAY_HOST + "/api/name/random")
                .addHeaders(getHeaderMap(null))
                .execute();
        String result = response.body();
        System.out.println(result);
        return result;
    }

    public String getYiYan(){
        HttpResponse response = HttpRequest.get(GATEWAY_HOST + "/api/yiyan")
                .addHeaders(getHeaderMap(null))
                .execute();
        String result = response.body();
        System.out.println(result);
        return result;
    }

    public String getDYgirl(){
        HttpResponse response = HttpRequest.get(GATEWAY_HOST + "/api/dygirl")
                .addHeaders(getHeaderMap(null))
                .execute();
        String result = response.body();
        System.out.println(result);
        return result;
    }

    public String getQQImage(String qq){
        HttpResponse response = HttpRequest.get(GATEWAY_HOST + "/api/qqimage?qq="+qq)
                .addHeaders(getHeaderMap(null))
                .execute();
        String result = response.body();
        System.out.println(result);
        return result;
    }

    public String getDuJiTang(){
        HttpResponse response = HttpRequest.get(GATEWAY_HOST + "/api/dujitang")
                .addHeaders(getHeaderMap(null))
                .execute();
        String result = response.body();
        System.out.println(result);
        return result;
    }



    private Map getHeaderMap(String body) {
        Map<String, String> hashmap = new HashMap<>();
        hashmap.put("accessKey", accessKey);
        //密钥不能发送给前端
        //hashmap.put("secretKey", secretKey);
        hashmap.put("nonce", RandomUtil.randomNumbers(4));
        hashmap.put("body", body);
        hashmap.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        hashmap.put("sign", SignUtils.getSign(body, secretKey));
        return hashmap;
    }


}
