package com.shortlink.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 扫描范围包含 common 模块（全局异常处理器等）
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.shortlink")
@MapperScan("com.shortlink.api.mapper")
public class ShortLinkApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShortLinkApiApplication.class, args);
    }
}
