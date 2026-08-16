package com.shortlink.gateway.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关鉴权配置（白名单）
 */
@Data
@Component
@ConfigurationProperties(prefix = "short-link.auth")
public class AuthProperties {

    private List<String> whiteList = new ArrayList<>();
}
