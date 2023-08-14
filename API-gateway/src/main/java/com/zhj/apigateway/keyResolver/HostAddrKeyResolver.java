package com.zhj.apigateway.keyResolver;

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

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        //根据ip地址限流
        return Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
    }

}
