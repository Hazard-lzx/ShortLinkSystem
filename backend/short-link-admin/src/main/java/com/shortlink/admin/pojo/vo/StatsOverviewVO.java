package com.shortlink.admin.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 访问统计总览
 */
@Data
public class StatsOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 短链总数 */
    private Long totalLinks;

    /** 累计访问总量 */
    private Long totalVisits;

    /** 今日访问量 */
    private Long todayVisits;
}
