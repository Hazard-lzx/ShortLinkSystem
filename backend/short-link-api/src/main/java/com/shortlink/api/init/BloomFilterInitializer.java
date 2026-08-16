package com.shortlink.api.init;

import com.shortlink.api.pojo.entity.ShortLink;
import com.shortlink.api.mapper.ShortLinkMapper;
import com.shortlink.api.properties.BloomProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.shortlink.common.constant.RedisKeyConstant.SHORT_LINK_BLOOM_FILTER;

/**
 * 布隆过滤器初始化器：服务启动时全量（游标分页）加载数据库短码
 * <p>tryInit：过滤器已存在时返回 false 且保留原数据；重复 add 同一短码是幂等操作
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BloomFilterInitializer implements ApplicationRunner {

    private static final int BATCH_SIZE = 1000;

    private final RedissonClient redissonClient;

    private final ShortLinkMapper shortLinkMapper;

    private final BloomProperties bloomProperties;

    @Override
    public void run(ApplicationArguments args) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(SHORT_LINK_BLOOM_FILTER);
        boolean firstInit = bloomFilter.tryInit(
                bloomProperties.getExpectedInsertions(), bloomProperties.getFalseProbability());
        log.info("布隆过滤器初始化：{}，预期容量 {}，误判率 {}",
                firstInit ? "首次创建" : "已存在（复用）",
                bloomProperties.getExpectedInsertions(), bloomProperties.getFalseProbability());

        long total = 0;
        long lastId = 0L;
        long start = System.currentTimeMillis();
        while (true) {
            List<ShortLink> batch = shortLinkMapper.selectCursorAfter(lastId, BATCH_SIZE);
            if (batch.isEmpty()) {
                break;
            }
            batch.forEach(link -> bloomFilter.add(link.getShortCode()));
            total += batch.size();
            lastId = batch.get(batch.size() - 1).getId();
            if (batch.size() < BATCH_SIZE) {
                break;
            }
        }
        log.info("布隆过滤器加载完成：共 {} 个短码，耗时 {} ms", total, System.currentTimeMillis() - start);
    }
}
