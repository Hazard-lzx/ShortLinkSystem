package com.shortlink.api.controller;

import cn.hutool.extra.servlet.JakartaServletUtil;
import com.shortlink.api.service.RedirectService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * 短链跳转接口（经网关 /s/{shortCode} 转发）
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class RedirectController {

    private static final String HEADER_USER_AGENT = "User-Agent";

    private final RedirectService redirectService;

    @GetMapping("/s/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable("shortCode") String shortCode,
                                         HttpServletRequest request) {
        String ip = JakartaServletUtil.getClientIP(request);
        String userAgent = request.getHeader(HEADER_USER_AGENT);
        String originalUrl = redirectService.getOriginalUrl(shortCode, ip, userAgent);
        log.info("短链跳转：{} -> {}", shortCode, originalUrl);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(originalUrl)).build();
    }
}
