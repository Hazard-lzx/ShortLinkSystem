package com.shortlink.gateway.config;

import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.HashSet;
import java.util.Set;

/**
 * Sentinel 网关限流配置
 *
 * <p>对短链跳转路由（资源名 = 路由 ID short-link-api）配置 QPS 限流，
 * 峰值流量直接在网关层拦截，保护后端跳转服务
 */
@Slf4j
@Configuration
public class SentinelGatewayConfig {

    private final String redirectQps;

    public SentinelGatewayConfig(@Value("${short-link-gateway.flow.redirect-qps:500}") String redirectQps) {
        this.redirectQps = redirectQps;
    }

    @PostConstruct
    public void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        // 跳转入口限流：每秒最大 QPS
        rules.add(new GatewayFlowRule("short-link-api")
                .setCount(Double.parseDouble(redirectQps))
                .setIntervalSec(1));
        GatewayRuleManager.loadRules(rules);

        // 限流后的响应内容：统一返回 JSON
        GatewayCallbackManager.setBlockHandler((exchange, throwable) -> {
            String body = JSONUtil.createObj()
                    .set("code", 429)
                    .set("message", "访问过于频繁，请稍后再试")
                    .set("success", false)
                    .toString();
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body);
        });
        log.info("Sentinel 网关限流规则已加载，跳转路由 [short-link-api] QPS 阈值：{}", redirectQps);
    }
}
