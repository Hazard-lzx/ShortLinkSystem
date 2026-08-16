package com.shortlink.admin.service;

import com.shortlink.admin.pojo.vo.StatsOverviewVO;
import com.shortlink.admin.pojo.vo.StatsTopVO;
import com.shortlink.admin.pojo.vo.StatsTrendVO;

import java.util.List;

/**
 * 访问统计服务
 */
public interface StatsService {

    /**访问统计总览*/
    StatsOverviewVO overview();

    /** 近 N 天访问趋势 */
    List<StatsTrendVO> trend(int days);

    /** 访问量 TopN 短链 */
    List<StatsTopVO> top(int limit);
}
