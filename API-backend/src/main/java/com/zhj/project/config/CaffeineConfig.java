package com.zhj.project.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zhj.common.model.entity.InterfaceInfo;
import com.zhj.common.model.entity.UserInterfaceInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * @author 朱焕杰
 * @version 1.0
 * @description TODO
 * @date 2023/4/13 20:22
 */
@Configuration
public class CaffeineConfig {

    @Bean
    public Cache<String, UserInterfaceInfo> userInterfaceInfoCache() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofHours(1))
                .build();
    }

    @Bean
    public Cache<String, InterfaceInfo> interfaceInfoCache() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofHours(1))
                .build();
    }

    @Bean
    public Cache<String, Integer> intCache() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofHours(1))
                .build();
    }

    @Bean
    public Cache<String, String> stringCache() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(10_000)
                .build();
    }
}
