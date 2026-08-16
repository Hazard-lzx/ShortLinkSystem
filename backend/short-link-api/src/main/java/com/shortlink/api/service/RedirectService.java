package com.shortlink.api.service;

/**
 * 短链跳转服务
 */
public interface RedirectService {

    /**
     * 根据短码获取原始链接（校验存在性/状态/有效期，并异步记录访问日志）
     *
     * @return 原始链接
     */
    String getOriginalUrl(String shortCode, String ip, String userAgent);
}
