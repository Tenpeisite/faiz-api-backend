package com.zhj.apiclientsdk;


import com.zhj.apiclientsdk.client.ApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author 朱焕杰
 * @version 1.0
 * @description TODO
 * @date 2023/4/4 20:18
 */
@Configuration
@ConfigurationProperties("faiz.api.client")
@Data
@ComponentScan
public class ApiClientConfig {
    private String accessKey;
    private String secretKey;

    @Bean
    public ApiClient apiClient() {
        return new ApiClient(accessKey, secretKey);
    }
}
