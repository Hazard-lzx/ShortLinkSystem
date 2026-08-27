package com.shortlink.admin.feign.fallback;

import com.shortlink.admin.feign.ShortLinkApiFeign;
import com.shortlink.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Feign 降级（工厂版）：跳转服务不可用时记录具体异常原因，且不阻塞管理端主业务，
 * 缓存一致性由缓存过期时间兜底（最终一致）
 */
@Slf4j
@Component
public class ShortLinkApiFeignFallback implements FallbackFactory<ShortLinkApiFeign> {

    @Override
    public ShortLinkApiFeign create(Throwable cause) {
        return new ShortLinkApiFeign() {

            @Override
            public Result<Void> addBloomFilter(String shortCode) {
                log.warn("调用跳转服务[布隆过滤器写入]降级：shortCode={}，异常原因：{}，新短链首次访问将回源查库重建缓存",
                        shortCode, cause.toString());
                return Result.fail("布隆过滤器写入降级");
            }

            @Override
            public Result<Void> evictCache(String shortCode) {
                log.warn("调用跳转服务[缓存清理]降级：shortCode={}，异常原因：{}，等待缓存自然过期实现最终一致",
                        shortCode, cause.toString());
                return Result.fail("缓存清理降级");
            }
        };
    }
}