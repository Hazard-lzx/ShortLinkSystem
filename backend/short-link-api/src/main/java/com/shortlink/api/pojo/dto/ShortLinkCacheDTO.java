package com.shortlink.api.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 短链缓存对象（Redis JSON）
 */
@Data
public class ShortLinkCacheDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String shortCode;

    /** 原始链接；数据库无记录时为空对象标记 {@code __EMPTY__} */
    private String originalUrl;

    /** 0-启用 1-禁用 */
    private Integer status;

    /** 过期时间（epoch 秒），NULL 表示永久有效 */
    private Long expireTime;
}
