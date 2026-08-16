package com.shortlink.api.service.impl;

import cn.hutool.json.JSONUtil;
import com.shortlink.api.pojo.dto.ShortLinkCacheDTO;
import com.shortlink.api.properties.CacheProperties;
import com.shortlink.api.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.shortlink.common.constant.CommonConstant.EMPTY_CACHE_MARK;
import static com.shortlink.common.constant.RedisKeyConstant.SHORT_LINK_CACHE_PREFIX;

/**
 * 短链缓存服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final StringRedisTemplate stringRedisTemplate;

    private final CacheProperties cacheProperties;

    /**
     * 获取短链缓存
     * @param shortCode 短码
     * @return 短链缓存对象，不存在时返回 null
     */
    @Override
    public ShortLinkCacheDTO get(String shortCode) {
        String json = stringRedisTemplate.opsForValue().get(SHORT_LINK_CACHE_PREFIX + shortCode);
        return json == null ? null : JSONUtil.toBean(json, ShortLinkCacheDTO.class);
    }

    /**
     * 设置短链缓存
     * <p>空缓存（短码不存在）使用较短的过期时间，正常缓存使用基础过期时间 + 随机偏移（防止缓存雪崩）
     * @param shortCode 短码
     * @param cacheDTO  短链缓存对象
     */
    @Override
    public void put(String shortCode, ShortLinkCacheDTO cacheDTO) {
        boolean empty = EMPTY_CACHE_MARK.equals(cacheDTO.getOriginalUrl());
        long ttl = empty
                ? cacheProperties.getEmptyCacheSeconds()
                : cacheProperties.getBaseExpireSeconds()
                        + ThreadLocalRandom.current().nextLong(cacheProperties.getRandomExpireSeconds() + 1);
        stringRedisTemplate.opsForValue()
                .set(SHORT_LINK_CACHE_PREFIX + shortCode, JSONUtil.toJsonStr(cacheDTO), ttl, TimeUnit.SECONDS);
    }

    /**
     * 删除短链缓存
     * @param shortCode 短码
     */
    @Override
    public void evict(String shortCode) {
        stringRedisTemplate.delete(SHORT_LINK_CACHE_PREFIX + shortCode);
        log.info("短链缓存已清理：{}", shortCode);
    }
}