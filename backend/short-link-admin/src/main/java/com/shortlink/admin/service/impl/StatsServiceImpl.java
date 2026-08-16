package com.shortlink.admin.service.impl;

import com.shortlink.admin.pojo.vo.StatsOverviewVO;
import com.shortlink.admin.pojo.vo.StatsTopVO;
import com.shortlink.admin.pojo.vo.StatsTrendVO;
import com.shortlink.admin.mapper.ShortLinkMapper;
import com.shortlink.admin.mapper.StatsMapper;
import com.shortlink.admin.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 访问统计服务实现类
 */
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private static final int MAX_DAYS = 90;

    private static final int MAX_LIMIT = 100;

    private final StatsMapper statsMapper;

    private final ShortLinkMapper shortLinkMapper;

    /**
     * 获取统计总览数据
     * @return 统计总览对象，包含总短链数、总访问量和今日访问量
     */
    @Override
    public StatsOverviewVO overview() {
        StatsOverviewVO overview = new StatsOverviewVO();
        overview.setTotalLinks(shortLinkMapper.selectCount(null));
        overview.setTotalVisits(statsMapper.sumTotalVisits());
        overview.setTodayVisits(statsMapper.countVisitsSince(LocalDate.now().atStartOfDay()));
        return overview;
    }

    /**
     * 获取近 N 天访问趋势数据
     * @param days 查询天数，范围 1-90，超出范围则默认为 7 天
     * @return 访问趋势数据列表
     */
    @Override
    public List<StatsTrendVO> trend(int days) {
        if (days < 1 || days > MAX_DAYS) {
            days = 7;
        }
        LocalDateTime startTime = LocalDate.now().minusDays(days - 1L).atStartOfDay();
        return statsMapper.selectTrend(startTime);
    }

    /**
     * 获取访问量 TopN 短链
     * @param limit 返回数量，范围 1-100，超出范围则默认为 10
     * @return 访问量排名前 N 的短链列表
     */
    @Override
    public List<StatsTopVO> top(int limit) {
        if (limit < 1 || limit > MAX_LIMIT) {
            limit = 10;
        }
        return statsMapper.selectTop(limit);
    }
}