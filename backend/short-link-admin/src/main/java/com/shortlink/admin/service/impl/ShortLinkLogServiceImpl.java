package com.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.admin.pojo.entity.ShortLinkLog;
import com.shortlink.admin.mapper.ShortLinkLogMapper;
import org.springframework.stereotype.Service;

/**
 * 访问日志批量写入（供 Kafka 消费者调用）
 */
@Service
public class ShortLinkLogServiceImpl extends ServiceImpl<ShortLinkLogMapper, ShortLinkLog> {
}
