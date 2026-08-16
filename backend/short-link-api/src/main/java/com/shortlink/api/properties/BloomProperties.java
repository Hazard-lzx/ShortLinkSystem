package com.shortlink.api.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 布隆过滤器参数
 */
@Data
@Component
@ConfigurationProperties(prefix = "short-link.bloom")
public class BloomProperties {

    /** 预期插入量 */
    private Long expectedInsertions = 1000000L;

    /** 误判率 */
    private Double falseProbability = 0.01;
}
