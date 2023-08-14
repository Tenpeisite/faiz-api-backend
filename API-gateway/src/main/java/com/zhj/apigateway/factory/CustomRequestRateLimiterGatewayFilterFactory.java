package com.zhj.apigateway.factory;


import com.zhj.common.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author 朱焕杰
 * @version 1.0
 * @description 自定义限流返回结果
 * @date 2023/6/20 15:32
 */
@Slf4j
@Component
public class CustomRequestRateLimiterGatewayFilterFactory extends RequestRateLimiterGatewayFilterFactory {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final RateLimiter defaultRateLimiter;

    private final KeyResolver defaultKeyResolver;

    public CustomRequestRateLimiterGatewayFilterFactory(RateLimiter defaultRateLimiter, KeyResolver defaultKeyResolver) {
        super(defaultRateLimiter, defaultKeyResolver);
        this.defaultRateLimiter = defaultRateLimiter;
        this.defaultKeyResolver = defaultKeyResolver;
    }

    @Override
    public GatewayFilter apply(Config config) {
        KeyResolver resolver = getOrDefault(config.getKeyResolver(), defaultKeyResolver);
        RateLimiter<Object> limiter = getOrDefault(config.getRateLimiter(), defaultRateLimiter);
        return (exchange, chain) -> resolver.resolve(exchange).flatMap(key -> {
            String routeId = config.getRouteId();
            if (routeId == null) {
                Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                routeId = route.getId();
            }
            String finalRouteId = routeId;
            return limiter.isAllowed(routeId, key).flatMap(response -> {
                for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
                    exchange.getResponse().getHeaders().add(header.getKey(), header.getValue());
                }
                if (response.isAllowed()) {
                    return chain.filter(exchange);
                }
                log.warn("已限流: {}", finalRouteId);
                //将ip地址加入黑名单
                String ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
                stringRedisTemplate.opsForZSet().add(RedisConstant.BLACK_LIST,ip,System.currentTimeMillis());
                //修改code为500
                ServerHttpResponse httpResponse = exchange.getResponse();
                httpResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                if (!httpResponse.getHeaders().containsKey("Content-Type")) {
                    httpResponse.getHeaders().add("Content-Type", "application/json");
                }
                //此处无法触发全局异常处理，手动返回
                DataBuffer buffer = httpResponse.bufferFactory().wrap(("{\n"
                        + "  \"code\": \"1414\","
                        + "  \"message\": \"服务器限流\","
                        + "  \"data\": \"Server throttling\","
                        + "  \"success\": false"
                        + "}").getBytes(StandardCharsets.UTF_8));
                return httpResponse.writeWith(Mono.just(buffer));
            });
        });
    }

    private <T> T getOrDefault(T configValue, T defaultValue) {
        return (configValue != null) ? configValue : defaultValue;
    }
}

