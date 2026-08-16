package com.shortlink.api.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 缓存参数（支持 Nacos 动态刷新）
 */
@Data
@Component
@ConfigurationProperties(prefix = "short-link.cache")
public class CacheProperties {

    /** 正常缓存基础过期时间（秒） */
    private Long baseExpireSeconds = 1800L;

    /** 随机过期增量（秒） */
    private Long randomExpireSeconds = 600L;

    /** 空对象缓存时间（秒） */
    private Long emptyCacheSeconds = 60L;
}
