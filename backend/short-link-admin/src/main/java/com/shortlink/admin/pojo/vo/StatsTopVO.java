package com.shortlink.admin.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 访问量 TopN 短链
 */
@Data
public class StatsTopVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String shortCode;

    private String originalUrl;

    private Long visitCount;
}
