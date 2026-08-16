package com.shortlink.admin.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 短链信息响应
 */
@Data
public class ShortLinkVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String shortCode;

    private String originalUrl;

    /** yyyy-MM-dd HH:mm:ss，NULL 表示永久有效 */
    private String expireTime;

    /** 0-启用 1-禁用 */
    private Integer status;

    private Long visitCount;

    private String createTime;

    private String updateTime;
}
