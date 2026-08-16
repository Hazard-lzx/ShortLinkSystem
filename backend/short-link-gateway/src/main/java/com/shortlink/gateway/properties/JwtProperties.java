package com.shortlink.gateway.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 网关 JWT 配置（与 short-link-admin 签发密钥保持一致）
 */
@Data
@Component
@ConfigurationProperties(prefix = "short-link.jwt")
public class JwtProperties {

    private String secret;

    private Long expireHour = 72L;
}
