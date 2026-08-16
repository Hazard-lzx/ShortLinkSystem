package com.shortlink.common.constant;

/**
 * Kafka Topic 常量
 */
public interface KafkaTopicConstant {

    /** 短链访问日志 Topic（api 生产 → admin 消费） */
    String VISIT_LOG_TOPIC = "short-link-visit-log";
}
