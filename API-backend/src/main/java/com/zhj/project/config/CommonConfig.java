package com.zhj.project.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.client.RestTemplate;

/**
 * @author zhj
 * @version 1.0
 * @date 2023/8/7 17:06
 */
@Configuration
@EnableAspectJAutoProxy(exposeProxy = true)
public class CommonConfig {

    @Bean
    @LoadBalanced
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
