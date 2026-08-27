package com.shortlink.admin.feign;

import com.shortlink.admin.feign.fallback.ShortLinkApiFeignFallback;
import com.shortlink.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 跳转服务内部接口（缓存一致性维护）
 *
 * <p>唯一调用场景：管理服务变更短链后通知跳转服务清理缓存 / 写入布隆过滤器；
 * 降级策略：失败仅记日志，依赖缓存过期时间保证最终一致，不阻塞主业务
 */
@FeignClient(
        name = "short-link-api",
        path = "/internal/cache",
        fallbackFactory = ShortLinkApiFeignFallback.class)
public interface ShortLinkApiFeign {

    /** 新短链创建后写入布隆过滤器 */
    @PostMapping("/bloom/add")
    Result<Void> addBloomFilter(@RequestParam("shortCode") String shortCode);

    /** 短链变更/删除后清理缓存 */
    @DeleteMapping("/evict")
    Result<Void> evictCache(@RequestParam("shortCode") String shortCode);
}
