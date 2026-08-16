package com.shortlink.admin.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 访问趋势
 */
@Data
public class StatsTrendVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日期 yyyy-MM-dd */
    private String statDate;

    private Long visitCount;
}
