package com.shortlink.api.service;

import com.shortlink.api.pojo.dto.ShortLinkCacheDTO;

/**
 * 短链缓存服务
 */
public interface CacheService {

    /** 读取缓存，未命中返回 null */
    ShortLinkCacheDTO get(String shortCode);

    /**
     * 写入缓存
     * <p>正常数据：基础过期时间 + 随机增量（防雪崩）
     * <p>空对象（数据库无记录，布隆误判兜底）：短过期时间
     */
    void put(String shortCode, ShortLinkCacheDTO cacheDTO);

    /** 删除缓存（短链变更/删除时由管理服务通过 Feign 调用） */
    void evict(String shortCode);
}
