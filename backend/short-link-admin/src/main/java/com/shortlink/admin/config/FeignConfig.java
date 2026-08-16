package com.shortlink.admin.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 容错配置：失败最多重试 1 次（首调 + 1 重试 = maxAttempts 2）
 */
@Configuration
public class FeignConfig {

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100, 200, 2);
    }
}
