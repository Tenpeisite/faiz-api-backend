package com.zhj.apigateway.keyResolver;

import com.zhj.common.model.entity.User;
import com.zhj.common.service.InnerUserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author 朱焕杰
 * @version 1.0
 * @description 限流规则
 * @date 2023/6/19 20:39
 */
@Component
public class HostAddrKeyResolver implements KeyResolver {

    @DubboReference
    private InnerUserService innerUserService;

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        String accessKey = exchange.getRequest().getHeaders().getFirst("accessKey");
        User user = innerUserService.getInvokeUser(accessKey);
        //根据用户id地址限流
        return Mono.just(user.getId()+"");
    }

}
