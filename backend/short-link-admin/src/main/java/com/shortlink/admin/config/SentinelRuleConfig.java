package com.shortlink.admin.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务级限流：统计类非核心接口限流，压力大时由 blockHandler 返回兜底数据，
 * 不影响短链管理与跳转核心链路
 */
@Slf4j
@Configuration
public class SentinelRuleConfig {

    private final double statsQps;

    public SentinelRuleConfig(@Value("${short-link-admin.flow.stats-qps:50}") double statsQps) {
        this.statsQps = statsQps;
    }

    @PostConstruct
    public void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();
        for (String resource : List.of("stats-overview", "stats-trend", "stats-top")) {
            FlowRule rule = new FlowRule(resource);
            rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
            rule.setCount(statsQps);
            rules.add(rule);
        }
        FlowRuleManager.loadRules(rules);
        log.info("统计接口限流规则已加载：QPS 阈值 {}", statsQps);
    }
}
