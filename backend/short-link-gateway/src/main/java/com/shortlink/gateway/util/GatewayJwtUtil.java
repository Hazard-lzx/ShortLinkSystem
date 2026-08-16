package com.shortlink.gateway.util;

import cn.hutool.jwt.JWTUtil;

import java.nio.charset.StandardCharsets;

/**
 * 网关 JWT 校验工具类
 */
public final class GatewayJwtUtil {

    private GatewayJwtUtil() {
    }

    /** 验证 JWT Token 的有效性*/
    public static boolean verify(String token, String secret) {
        try {
            return JWTUtil.verify(token, secret.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    /**从 JWT Token 中解析用户名*/
    public static String getUsername(String token, String secret) {
        return JWTUtil.parseToken(token).getPayload("username").toString();
    }
}