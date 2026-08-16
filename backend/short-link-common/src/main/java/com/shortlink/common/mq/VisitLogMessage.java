package com.shortlink.common.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 短链访问日志消息（short-link-api 生产 → short-link-admin 消费）
 *
 * <p>时间为格式化字符串（yyyy-MM-dd HH:mm:ss），避免跨服务 JSON 序列化差异
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitLogMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 短码 */
    private String shortCode;

    /** 访问者 IP */
    private String ip;

    /** 访问者 User-Agent */
    private String userAgent;

    /** 访问时间 yyyy-MM-dd HH:mm:ss */
    private String visitTime;
}
