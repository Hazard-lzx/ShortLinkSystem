package com.shortlink.common.constant;

/**
 * Redis Key 常量
 */
public interface RedisKeyConstant {

    /** 短链缓存 Key 前缀：short:link:cache:{shortCode} → JSON(ShortLinkCacheDTO) */
    String SHORT_LINK_CACHE_PREFIX = "short:link:cache:";

    /** 缓存重建互斥锁前缀：short:link:lock:{shortCode} */
    String SHORT_LINK_LOCK_PREFIX = "short:link:lock:";

    /** 布隆过滤器名称 */
    String SHORT_LINK_BLOOM_FILTER = "short:link:bloom:filter";
}
