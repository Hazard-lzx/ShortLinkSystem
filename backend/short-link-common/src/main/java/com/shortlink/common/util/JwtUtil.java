package com.shortlink.common.util;

import cn.hutool.jwt.JWTUtil;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类（HS256，基于 Hutool JWT，网关与管理服务共用同一密钥）
 */
public class JwtUtil {

    public static final String CLAIM_USERNAME = "username";

    private JwtUtil() {
    }

    /**
     * 签发 Token
     *
     * @param username   用户名
     * @param secret     签名密钥（与网关保持一致）
     * @param expireHour 有效时长（小时）
     */
    public static String createToken(String username, String secret, long expireHour) {
        Map<String, Object> payload = new HashMap<>(4);
        payload.put(CLAIM_USERNAME, username);
        payload.put("iat", System.currentTimeMillis() / 1000);
        payload.put("exp", System.currentTimeMillis() / 1000 + expireHour * 3600);
        return JWTUtil.createToken(payload, secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 校验 Token 签名与有效期
     */
    public static boolean verify(String token, String secret) {
        return JWTUtil.verify(token, secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 从 Token 中解析用户名（不校验签名，签名校验请先调用 {@link #verify}）
     */
    public static String getUsername(String token, String secret) {
        return JWTUtil.parseToken(token).getPayload(CLAIM_USERNAME).toString();
    }
}
