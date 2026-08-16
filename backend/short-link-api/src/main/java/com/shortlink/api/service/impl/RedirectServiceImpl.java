package com.shortlink.api.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shortlink.api.pojo.dto.ShortLinkCacheDTO;
import com.shortlink.api.pojo.entity.ShortLink;
import com.shortlink.api.mapper.ShortLinkMapper;
import com.shortlink.api.mq.VisitLogProducer;
import com.shortlink.api.service.CacheService;
import com.shortlink.api.service.RedirectService;
import com.shortlink.common.constant.CommonConstant;
import com.shortlink.common.constant.RedisKeyConstant;
import com.shortlink.common.exception.BizException;
import com.shortlink.common.mq.VisitLogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import static com.shortlink.common.constant.CommonConstant.EMPTY_CACHE_MARK;
import static com.shortlink.common.constant.RedisKeyConstant.SHORT_LINK_BLOOM_FILTER;
import static com.shortlink.common.constant.RedisKeyConstant.SHORT_LINK_LOCK_PREFIX;

/**
 * 跳转核心逻辑：布隆过滤器 → Redis 缓存 → 互斥锁重建缓存 → 302 跳转
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedirectServiceImpl implements RedirectService {

    private static final long LOCK_WAIT_SECONDS = 0;

    private static final long LOCK_LEASE_SECONDS = 5;

    private static final long RETRY_READ_WAIT_MILLIS = 100;

    private final RedissonClient redissonClient;

    private final ShortLinkMapper shortLinkMapper;

    private final CacheService cacheService;

    private final VisitLogProducer visitLogProducer;

    @Override
    public String getOriginalUrl(String shortCode, String ip, String userAgent) {
        // 1. 布隆过滤器拦截不存在的短码，解决缓存穿透
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(SHORT_LINK_BLOOM_FILTER);
        if (!bloomFilter.contains(shortCode)) {
            throw new BizException(404, "短链不存在或已被删除");
        }

        // 2. 查询缓存
        ShortLinkCacheDTO cacheDTO = cacheService.get(shortCode);

        // 3. 未命中 → 互斥锁重建，解决缓存击穿
        if (cacheDTO == null) {
            cacheDTO = rebuildCache(shortCode);
        }

        // 4. 空对象缓存：布隆过滤器误判，视作不存在
        if (EMPTY_CACHE_MARK.equals(cacheDTO.getOriginalUrl())) {
            throw new BizException(404, "短链不存在或已被删除");
        }

        // 5. 校验状态与有效期
        if (CommonConstant.STATUS_DISABLE.equals(cacheDTO.getStatus())) {
            throw new BizException(403, "短链已被禁用");
        }
        if (cacheDTO.getExpireTime() != null
                && cacheDTO.getExpireTime() < System.currentTimeMillis() / 1000) {
            throw new BizException(410, "短链已过期");
        }

        // 6. 异步记录访问日志（Kafka），不阻塞跳转主链路
        visitLogProducer.send(VisitLogMessage.builder()
                .shortCode(shortCode)
                .ip(ip)
                .userAgent(userAgent)
                .visitTime(DateUtil.now())
                .build());

        return cacheDTO.getOriginalUrl();
    }

    /**
     * 缓存重建：同一短码只允许一个请求回源查库，其余请求短暂等待后重读缓存
     */
    private ShortLinkCacheDTO rebuildCache(String shortCode) {
        RLock lock = redissonClient.getLock(SHORT_LINK_LOCK_PREFIX + shortCode);
        boolean locked = false;
        try {
            locked = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (locked) {
                // 双重检查：拿到锁后可能已被其他线程重建
                ShortLinkCacheDTO cacheDTO = cacheService.get(shortCode);
                if (cacheDTO != null) {
                    return cacheDTO;
                }
                ShortLink shortLink = shortLinkMapper.selectOne(
                        new LambdaQueryWrapper<ShortLink>().eq(ShortLink::getShortCode, shortCode));
                ShortLinkCacheDTO rebuilt = toCacheDTO(shortLink);
                cacheService.put(shortCode, rebuilt);
                return rebuilt;
            }
            // 未抢到锁：等待重建完成后重读缓存
            Thread.sleep(RETRY_READ_WAIT_MILLIS);
            ShortLinkCacheDTO cacheDTO = cacheService.get(shortCode);
            if (cacheDTO != null) {
                return cacheDTO;
            }
            throw new BizException(500, "系统繁忙，请稍后重试");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(500, "系统繁忙，请稍后重试");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private ShortLinkCacheDTO toCacheDTO(ShortLink shortLink) {
        ShortLinkCacheDTO dto = new ShortLinkCacheDTO();
        if (shortLink == null) {
            // 数据库无记录（布隆误判或已被删除）：写入空对象短缓存
            dto.setOriginalUrl(EMPTY_CACHE_MARK);
            return dto;
        }
        dto.setShortCode(shortLink.getShortCode());
        dto.setOriginalUrl(shortLink.getOriginalUrl());
        dto.setStatus(shortLink.getStatus());
        LocalDateTime expireTime = shortLink.getExpireTime();
        dto.setExpireTime(expireTime == null ? null
                : expireTime.atZone(ZoneId.systemDefault()).toEpochSecond());
        return dto;
    }
}
