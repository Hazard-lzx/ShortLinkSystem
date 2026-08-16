package com.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shortlink.admin.pojo.dto.ShortLinkCreateDTO;
import com.shortlink.admin.pojo.dto.ShortLinkPageDTO;
import com.shortlink.admin.pojo.dto.ShortLinkUpdateDTO;
import com.shortlink.admin.pojo.vo.ShortLinkCreateVO;
import com.shortlink.admin.pojo.vo.ShortLinkVO;
import com.shortlink.admin.pojo.entity.ShortLink;
import com.shortlink.admin.feign.ShortLinkApiFeign;
import com.shortlink.admin.mapper.ShortLinkMapper;
import com.shortlink.admin.service.ShortLinkService;
import com.shortlink.common.constant.CommonConstant;
import com.shortlink.common.exception.BizException;
import com.shortlink.common.result.PageResult;
import com.shortlink.common.util.ShortCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 短链管理实现
 * <p>写链路与跳转服务的缓存一致性：所有变更操作完成后通过 Feign 通知跳转服务清理缓存，
 * Feign 降级时由缓存过期时间兜底（最终一致）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl implements ShortLinkService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final int MAX_RETRY = 3;

    private final ShortLinkMapper shortLinkMapper;

    private final ShortLinkApiFeign shortLinkApiFeign;

    /**
     * 创建短链
     * @param createDTO
     * @return
     */
    @Override
    public ShortLinkCreateVO createShortLink(ShortLinkCreateDTO createDTO) {
        ShortLink shortLink = new ShortLink();
        shortLink.setOriginalUrl(createDTO.getOriginalUrl());
        shortLink.setExpireTime(parseExpireTime(createDTO.getExpireTime()));
        shortLink.setStatus(com.shortlink.common.constant.CommonConstant.STATUS_ENABLE);

        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            shortLink.setShortCode(ShortCodeGenerator.generate());
            try {
                shortLinkMapper.insert(shortLink);
                break;
            } catch (DuplicateKeyException e) {
                // 雪花 ID 短码理论不重复，唯一索引冲突兜底重试
                if (attempt == MAX_RETRY) {
                    throw new BizException("短码生成冲突，请重试");
                }
            }
        }

        // 通知跳转服务写入布隆过滤器（降级时首次访问回源查库）
        shortLinkApiFeign.addBloomFilter(shortLink.getShortCode());
        log.info("短链创建成功：{} -> {}", shortLink.getShortCode(), shortLink.getOriginalUrl());

        ShortLinkCreateVO createVO = new ShortLinkCreateVO();
        createVO.setShortCode(shortLink.getShortCode());
        return createVO;
    }


    /**
     * 更新短链
     * @param updateDTO
     */
    @Override
    public void updateShortLink(ShortLinkUpdateDTO updateDTO) {
        requireExists(updateDTO.getShortCode());
        LambdaUpdateWrapper<ShortLink> wrapper = new LambdaUpdateWrapper<ShortLink>()
                .eq(ShortLink::getShortCode, updateDTO.getShortCode())
                .set(ShortLink::getOriginalUrl, updateDTO.getOriginalUrl())
                .set(ShortLink::getExpireTime, parseExpireTime(updateDTO.getExpireTime()));
        shortLinkMapper.update(null, wrapper);
        shortLinkApiFeign.evictCache(updateDTO.getShortCode());
        log.info("短链已更新：{}", updateDTO.getShortCode());
    }

    /**
     * 修改短链状态
     * @param shortCode
     * @param status
     */
    @Override
    public void updateStatus(String shortCode, Integer status) {
        if (!CommonConstant.STATUS_ENABLE.equals(status)
                && !CommonConstant.STATUS_DISABLE.equals(status)) {
            throw new BizException("非法状态值，仅支持 0-启用 1-禁用");
        }
        requireExists(shortCode);
        LambdaUpdateWrapper<ShortLink> wrapper = new LambdaUpdateWrapper<ShortLink>()
                .eq(ShortLink::getShortCode, shortCode)
                .set(ShortLink::getStatus, status);
        shortLinkMapper.update(null, wrapper);
        shortLinkApiFeign.evictCache(shortCode);
        log.info("短链状态已变更：{} -> {}", shortCode, status);
    }

    /**
     * 删除短链
     * @param shortCode
     */
    @Override
    public void deleteShortLink(String shortCode) {
        requireExists(shortCode);
        shortLinkMapper.delete(new LambdaQueryWrapper<ShortLink>().eq(ShortLink::getShortCode, shortCode));
        // 布隆过滤器不支持删除，残留短码由跳转服务的空对象缓存兜底（短 TTL）
        shortLinkApiFeign.evictCache(shortCode);
        log.info("短链已删除：{}", shortCode);
    }

    /**
     * 短码查详情
     * @param shortCode
     * @return
     */
    @Override
    public ShortLinkVO getShortLink(String shortCode) {
        ShortLink shortLink = shortLinkMapper.selectOne(
                new LambdaQueryWrapper<ShortLink>().eq(ShortLink::getShortCode, shortCode));
        if (shortLink == null) {
            throw new BizException(404, "短链不存在");
        }
        return toRespDTO(shortLink);
    }

    /**
     * 分页条件查询
     * @param pageDTO
     * @return
     */
    @Override
    public PageResult<ShortLinkVO> pageShortLink(ShortLinkPageDTO pageDTO) {
        LambdaQueryWrapper<ShortLink> wrapper = new LambdaQueryWrapper<ShortLink>()
                .eq(StrUtil.isNotBlank(pageDTO.getShortCode()), ShortLink::getShortCode, pageDTO.getShortCode())
                .like(StrUtil.isNotBlank(pageDTO.getOriginalUrl()), ShortLink::getOriginalUrl, pageDTO.getOriginalUrl())
                .eq(pageDTO.getStatus() != null, ShortLink::getStatus, pageDTO.getStatus())
                .orderByDesc(ShortLink::getId);

        Page<ShortLink> page = shortLinkMapper.selectPage(
                new Page<>(pageDTO.getCurrent(), pageDTO.getSize()), wrapper);

        List<ShortLinkVO> records = page.getRecords().stream().map(this::toRespDTO).toList();
        return PageResult.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    private void requireExists(String shortCode) {
        Long count = shortLinkMapper.selectCount(
                new LambdaQueryWrapper<ShortLink>().eq(ShortLink::getShortCode, shortCode));
        if (count == null || count == 0) {
            throw new BizException(404, "短链不存在");
        }
    }

    private LocalDateTime parseExpireTime(String expireTime) {
        if (StrUtil.isBlank(expireTime)) {
            return null;
        }
        try {
            return LocalDateTimeUtil.parse(expireTime, FORMATTER);
        } catch (Exception e) {
            throw new BizException("过期时间格式错误，应为 yyyy-MM-dd HH:mm:ss");
        }
    }

    private ShortLinkVO toRespDTO(ShortLink shortLink) {
        ShortLinkVO linkVO = new ShortLinkVO();
        BeanUtil.copyProperties(shortLink, linkVO, "expireTime", "createTime", "updateTime");
        if (shortLink.getExpireTime() != null) {
            linkVO.setExpireTime(shortLink.getExpireTime().format(FORMATTER));
        }
        if (shortLink.getCreateTime() != null) {
            linkVO.setCreateTime(shortLink.getCreateTime().format(FORMATTER));
        }
        if (shortLink.getUpdateTime() != null) {
            linkVO.setUpdateTime(shortLink.getUpdateTime().format(FORMATTER));
        }
        return linkVO;
    }
}
