package com.shortlink.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 扫描范围包含 common 模块（全局异常处理器等）
 */
@EnableFeignClients(basePackages = "com.shortlink.admin.feign")
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.shortlink")
@MapperScan("com.shortlink.admin.mapper")
public class ShortLinkAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShortLinkAdminApplication.class, args);
    }
}
