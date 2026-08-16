package com.shortlink.admin.mq;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.shortlink.admin.pojo.entity.ShortLinkLog;
import com.shortlink.admin.mapper.ShortLinkMapper;
import com.shortlink.admin.service.impl.ShortLinkLogServiceImpl;
import com.shortlink.common.constant.KafkaTopicConstant;
import com.shortlink.common.mq.VisitLogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 访问日志消费者：批量消费 → 批量写日志表 + 累计更新访问次数
 *
 * <p>可靠性：手动提交 Offset（批量处理成功后 ack）；
 * 处理异常记录日志并 ack 跳过（生产环境可升级为死信队列重试）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VisitLogConsumer {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ShortLinkLogServiceImpl shortLinkLogService;

    private final ShortLinkMapper shortLinkMapper;

    @KafkaListener(topics = KafkaTopicConstant.VISIT_LOG_TOPIC)
    public void onMessage(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        if (records.isEmpty()) {
            ack.acknowledge();
            return;
        }
        try {
            List<ShortLinkLog> logs = new ArrayList<>(records.size());
            Map<String, Long> visitCountMap = new HashMap<>();
            for (ConsumerRecord<String, String> record : records) {
                VisitLogMessage message = JSONUtil.toBean(record.value(), VisitLogMessage.class);
                if (StrUtil.isBlank(message.getShortCode())) {
                    continue;
                }
                ShortLinkLog shortLinkLog = new ShortLinkLog();
                shortLinkLog.setShortCode(message.getShortCode());
                shortLinkLog.setIp(message.getIp());
                shortLinkLog.setUserAgent(message.getUserAgent());
                shortLinkLog.setVisitTime(LocalDateTime.parse(message.getVisitTime(), FORMATTER));
                logs.add(shortLinkLog);
                visitCountMap.merge(message.getShortCode(), 1L, Long::sum);
            }
            // 批量写入日志表
            shortLinkLogService.saveBatch(logs);
            // 按短码聚合累计访问次数
            visitCountMap.forEach(shortLinkMapper::incrVisitCount);
            log.info("批量消费访问日志：{} 条，涉及 {} 个短码", logs.size(), visitCountMap.size());
        } catch (Exception e) {
            log.error("访问日志批量入库失败，本批次跳过（生产环境应转入死信队列）：{}", records.size(), e);
        } finally {
            ack.acknowledge();
        }
    }
}
