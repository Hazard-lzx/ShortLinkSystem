package com.shortlink.api.mq;

import cn.hutool.json.JSONUtil;
import com.shortlink.common.constant.KafkaTopicConstant;
import com.shortlink.common.mq.VisitLogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 访问日志消息生产者：跳转成功后异步发送，失败仅记录日志，不影响跳转
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VisitLogProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void send(VisitLogMessage message) {
        String json = JSONUtil.toJsonStr(message);
        kafkaTemplate.send(KafkaTopicConstant.VISIT_LOG_TOPIC, message.getShortCode(), json)
                .whenComplete((result, e) -> {
                    if (e != null) {
                        log.error("访问日志消息发送失败 shortCode={}：{}", message.getShortCode(), e.getMessage());
                    }
                });
    }
}
