package com.shortlink.admin.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 短链访问日志（Kafka 批量消费写入）
 */
@Data
@TableName("t_short_link_log")
public class ShortLinkLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String shortCode;

    private String ip;

    private String userAgent;

    private LocalDateTime visitTime;
}
