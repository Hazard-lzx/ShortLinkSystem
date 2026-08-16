package com.shortlink.gateway.filter;

import cn.hutool.core.util.StrUtil;
import com.shortlink.gateway.properties.AuthProperties;
import com.shortlink.gateway.properties.JwtProperties;
import com.shortlink.gateway.util.GatewayJwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static com.shortlink.gateway.filter.AuthTokenGlobalFilter.HttpHeadersConstant.HEADER_AUTHORIZATION;
import static com.shortlink.gateway.filter.AuthTokenGlobalFilter.HttpHeadersConstant.HEADER_USERNAME;
import static com.shortlink.gateway.filter.AuthTokenGlobalFilter.HttpHeadersConstant.TOKEN_PREFIX;

/**
 * JWT 统一鉴权全局过滤器
 * <p>白名单直接放行；管理类接口校验 Token，
 * 校验通过后将用户名透传给下游服务（请求头 X-Username）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthTokenGlobalFilter implements GlobalFilter, Ordered {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private static final String UNAUTHORIZED_BODY = "{\"code\":401,\"message\":\"未登录或登录已过期\",\"success\":false}";

    private final AuthProperties authProperties;

    private final JwtProperties jwtProperties;

    /**
     * 核心过滤逻辑：对请求进行鉴权处理
     * @param exchange 当前请求的上下文，包含请求和响应对象
     * @param chain    过滤器链，用于将请求传递给下一个过滤器
     * @return Mono<Void> 响应式处理结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 白名单路径直接放行
        if (isWhiteList(path)) {
            return chain.filter(exchange);
        }

        // 提取并验证 Token
        String token = resolveToken(request.getHeaders());
        if (StrUtil.isBlank(token) || !GatewayJwtUtil.verify(token, jwtProperties.getSecret())) {
            return writeUnauthorized(exchange.getResponse());
        }

        // Token 验证通过，解析用户名并透传给下游服务
        String username = GatewayJwtUtil.getUsername(token, jwtProperties.getSecret());
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(HEADER_USERNAME, username)
                .build();
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    /**
     * 判断请求路径是否在白名单中
     * @param path 请求路径
     * @return 路径在白名单中返回 true，否则返回 false
     */
    private boolean isWhiteList(String path) {
        return authProperties.getWhiteList().stream().anyMatch(pattern -> MATCHER.match(pattern, path));
    }

    /**
     * 从请求头中解析 JWT Token
     * @param headers HTTP 请求头
     * @return JWT Token 字符串，如果未提供或格式错误则返回 null
     */
    private String resolveToken(HttpHeaders headers) {
        String authorization = headers.getFirst(HEADER_AUTHORIZATION);
        if (StrUtil.isBlank(authorization)) {
            return null;
        }
        return StrUtil.removePrefix(authorization, TOKEN_PREFIX).trim();
    }

    /**
     * 返回 401 未授权响应
     * @param response HTTP 响应对象
     * @return Mono<Void> 响应式写入结果
     */
    private Mono<Void> writeUnauthorized(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        org.springframework.core.io.buffer.DataBuffer buffer =
                response.bufferFactory().wrap(UNAUTHORIZED_BODY.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 获取过滤器执行顺序
     * @return 过滤器优先级顺序值
     */
    @Override
    public int getOrder() {
        return -100;
    }

    /**
     * HTTP 请求头常量定义
     */
    static final class HttpHeadersConstant {

        /** Authorization 请求头，用于携带 JWT Token */
        static final String HEADER_AUTHORIZATION = "Authorization";

        /** X-Username 请求头，用于向下游服务透传用户名 */
        static final String HEADER_USERNAME = "X-Username";

        /** Token 前缀，标准的 Bearer Token 格式 */
        static final String TOKEN_PREFIX = "Bearer ";

        /** 私有构造函数，防止实例化 */
        private HttpHeadersConstant() {
        }
    }
}