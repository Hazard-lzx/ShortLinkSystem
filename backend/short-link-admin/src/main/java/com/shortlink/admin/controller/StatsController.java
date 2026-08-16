package com.shortlink.admin.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.shortlink.admin.pojo.vo.StatsOverviewVO;
import com.shortlink.admin.pojo.vo.StatsTopVO;
import com.shortlink.admin.pojo.vo.StatsTrendVO;
import com.shortlink.admin.service.StatsService;
import com.shortlink.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * 访问统计：非核心接口，配置 Sentinel 限流降级，系统压力大时返回兜底数据
 */
@Tag(name = "访问统计")
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @Operation(summary = "统计总览")
    @GetMapping("/overview")
    @SentinelResource(value = "stats-overview", blockHandler = "overviewBlockHandler")
    public Result<StatsOverviewVO> overview() {
        return Result.success(statsService.overview());
    }

    @Operation(summary = "近 N 天访问趋势")
    @GetMapping("/trend")
    @SentinelResource(value = "stats-trend", blockHandler = "trendBlockHandler")
    public Result<List<StatsTrendVO>> trend(@RequestParam(value = "days", defaultValue = "7") Integer days) {
        return Result.success(statsService.trend(days));
    }

    @Operation(summary = "访问量 TopN 短链")
    @GetMapping("/top")
    @SentinelResource(value = "stats-top", blockHandler = "topBlockHandler")
    public Result<List<StatsTopVO>> top(@RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return Result.success(statsService.top(limit));
    }

    /** 限流兜底：返回空统计，保证页面可用 */
    public Result<StatsOverviewVO> overviewBlockHandler(BlockException e) {
        return Result.success(new StatsOverviewVO());
    }

    public Result<List<StatsTrendVO>> trendBlockHandler(Integer days, BlockException e) {
        return Result.success(Collections.emptyList());
    }

    public Result<List<StatsTopVO>> topBlockHandler(Integer limit, BlockException e) {
        return Result.success(Collections.emptyList());
    }
}
