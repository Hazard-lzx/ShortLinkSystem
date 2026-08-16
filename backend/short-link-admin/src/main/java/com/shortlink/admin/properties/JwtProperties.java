package com.shortlink.admin.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 签发配置（密钥与网关一致）
 */
@Data
@Component
@ConfigurationProperties(prefix = "short-link.jwt")
public class JwtProperties {

    private String secret;

    private Long expireHour = 72L;
}
