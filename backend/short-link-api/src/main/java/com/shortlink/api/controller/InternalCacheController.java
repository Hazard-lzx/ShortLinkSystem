package com.shortlink.api.controller;

import com.shortlink.api.service.CacheService;
import com.shortlink.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.shortlink.common.constant.RedisKeyConstant.SHORT_LINK_BLOOM_FILTER;

/**
 * 内部缓存维护接口：仅供 short-link-admin 通过 OpenFeign 服务名直调，
 * 网关未配置 /internal/** 路由，不对外网暴露
 */
@Slf4j
@RestController
@RequestMapping("/internal/cache")
@RequiredArgsConstructor
public class InternalCacheController {

    private final CacheService cacheService;

    private final RedissonClient redissonClient;

    /** 新短链创建后：写入布隆过滤器 */
    @PostMapping("/bloom/add")
    public Result<Void> addBloomFilter(@RequestParam("shortCode") String shortCode) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(SHORT_LINK_BLOOM_FILTER);
        bloomFilter.add(shortCode);
        log.info("布隆过滤器新增短码：{}", shortCode);
        return Result.success();
    }

    /** 短链修改/禁用/删除后：清理缓存，保证缓存一致性 */
    @DeleteMapping("/evict")
    public Result<Void> evictCache(@RequestParam("shortCode") String shortCode) {
        cacheService.evict(shortCode);
        return Result.success();
    }
}
