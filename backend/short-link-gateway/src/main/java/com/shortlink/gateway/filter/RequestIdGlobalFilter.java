package com.shortlink.gateway.filter;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局请求 ID 过滤器：为每个请求注入 X-Request-Id，方便全链路日志排查
 */
@Slf4j
@Component
public class RequestIdGlobalFilter implements GlobalFilter, Ordered {

    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestId = request.getHeaders().getFirst(HEADER_REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            requestId = IdUtil.fastSimpleUUID();
        }
        ServerHttpRequest mutatedRequest = request.mutate().header(HEADER_REQUEST_ID, requestId).build();
        log.info("[{}] {} {}", requestId, request.getMethod(), request.getURI().getPath());
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -200;
    }
}
