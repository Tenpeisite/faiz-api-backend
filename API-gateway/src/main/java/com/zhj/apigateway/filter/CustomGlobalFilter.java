package com.zhj.apigateway.filter;


import com.alibaba.fastjson.JSON;
import com.zhj.apiclientsdk.utis.SignUtils;
import com.zhj.common.constant.RedisConstant;
import com.zhj.common.model.entity.InterfaceInfo;
import com.zhj.common.model.entity.User;
import com.zhj.common.service.InnerInterfaceInfoService;
import com.zhj.common.service.InnerUserInterfaceInfoService;
import com.zhj.common.service.InnerUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * @author 朱焕杰
 * @version 1.0
 * @description TODO
 * @date 2023/4/7 21:41
 */
@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    @DubboReference
    private InnerUserService innerUserService;

    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;

    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1");

    private static final String INTERFACE_HOST = "http://localhost:8123";


    /**
     * 全局过滤器
     *
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.请求日志
        ServerHttpRequest request = exchange.getRequest();
        //接口路径
        String path = request.getPath().value();
        String method = request.getMethod().toString();
        log.info("请求唯一标识:" + request.getId());
        log.info("接口路径:" + path);
        log.info("请求方法:" + method);
        log.info("请求参数:" + request.getQueryParams());
        //log.info("请求ip和端口号:" + request.getRemoteAddress());
        String sourceAddress = request.getRemoteAddress().getAddress().getHostAddress();
        log.info("请求ip:" + sourceAddress);
        //2.访问控制 - 黑名单
        ServerHttpResponse response = exchange.getResponse();
        //获得ip地址在zset集合中的排名。
        //如果ip地址存在redis，则根据score返回排名；否则，返回null
        Long rank = stringRedisTemplate.opsForZSet().rank(RedisConstant.BLACK_LIST, sourceAddress);
        if (rank != null) {
            //在黑名单中
            return handleBlack(response);
        }
        //3.用户鉴权（判断 ak sk 是否合法）
        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        String nonce = headers.getFirst("nonce");
        String timestamp = headers.getFirst("timestamp");
        String sign = headers.getFirst("sign");
        String apiHeader = headers.getFirst("apiHeader");
        String first = headers.getFirst("X-My-Header");
        //增加一个请求头
        //request.mutate().header("test", "test").build();
        //String body = headers.getFirst("body");
        String body = StringUtils.isNotBlank(headers.getFirst("body")) ? headers.getFirst("body") : null;
        //防止重放-第一步(时间和当前时间不能超过5min)
        if (System.currentTimeMillis() / 1000 - Long.parseLong(timestamp) > 5 * 60) {
            return handleNoAuth(response);
        }
        //利用accesskey查询用户
        User invokeUser = null;
        try {
            invokeUser = innerUserService.getInvokeUser(accessKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (invokeUser == null) {
            return handleNoAuth(response);
        }
        //防止重放-第二步(随机数在redis中不能存在)
        if (StringUtils.isBlank(nonce) || stringRedisTemplate.hasKey(nonce)) {//判断是否包含随机数
            return handleNoAuth(response);
        }
        stringRedisTemplate.opsForValue().set(nonce, "", 5 * 60, TimeUnit.SECONDS);
        String secretKey = invokeUser.getSecretKey();
        String serverSign = SignUtils.getSign(body, secretKey);
        if (sign == null || !sign.equals(serverSign)) {
            return handleNoAuth(response);
        }
        //4.请求的模拟接口是否存在？
        InterfaceInfo interfoceInfo = null;
        try {
            interfoceInfo = innerInterfaceInfoService.getInterfoceInfo(path, method);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("getInterfaceInfo error", e);
        }
        if (interfoceInfo == null) {
            return handleNoAuth(response);
        }
        //todo 5.判断用户是否还有剩余调用次数
        //使用次数
        boolean flag = innerUserInterfaceInfoService.isNumOfUse(interfoceInfo.getId(), invokeUser.getId());
        if (!flag) {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            String responseBody = "调用次数已用完";
            return response.writeWith(Mono.just(response.bufferFactory().wrap(responseBody.getBytes())));
        }
        //5.请求转发，调用模拟接口+响应日志
        return handleResponse(exchange, chain, interfoceInfo.getId(), interfoceInfo.getUserId());
        //Mono<Void> filter = chain.filter(exchange);
        //return filter();
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private Mono<Void> handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    private Mono<Void> handleError(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }

    private Mono<Void> handleBlack(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        String responseBody = "ip被封了";
        DataBuffer buffer = response.bufferFactory().wrap(responseBody.getBytes());
        return response.writeWith(Flux.just(buffer));
    }

    /**
     * 装饰者模式
     *
     * @param exchange
     * @param chain
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, long interfaceInfoId, long userId) {
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            // 缓存数据的工厂
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            // 拿到响应码
            HttpStatus statusCode = originalResponse.getStatusCode();
            if (statusCode == HttpStatus.OK) {
                // 装饰，增强能力
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    // 等调用完转发的接口后才会执行
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            // 往返回值里写数据
                            // 拼接字符串
                            return super.writeWith(
                                    fluxBody.map(dataBuffer -> {
                                        // 7. 调用成功，接口调用次数 + 1 invokeCount
                                        try {
                                            boolean flag = innerUserInterfaceInfoService.invokeCount(interfaceInfoId, userId);
                                            log.info("<-------修改接口调用次数：{}", flag == true ? "成功" : "失败");
                                        } catch (Exception e) {
                                            log.error("invokeCount error", e);
                                        }
                                        byte[] content = new byte[dataBuffer.readableByteCount()];
                                        dataBuffer.read(content);
                                        DataBufferUtils.release(dataBuffer);//释放掉内存
                                        // 构建日志
                                        StringBuilder sb2 = new StringBuilder(200);
                                        List<Object> rspArgs = new ArrayList<>();
                                        rspArgs.add(originalResponse.getStatusCode());
                                        String data = new String(content, StandardCharsets.UTF_8); //data
                                        sb2.append(data);
                                        // 打印日志
                                        log.info("响应结果：" + data);
                                        return bufferFactory.wrap(content);
                                    }));
                        } else {
                            // 8. 调用失败，返回一个规范的错误码
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                // 设置 response 对象为装饰过的
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            return chain.filter(exchange); // 降级处理返回数据
        } catch (Exception e) {
            log.error("网关处理响应异常" + e);
            return chain.filter(exchange);
        }
    }
}
