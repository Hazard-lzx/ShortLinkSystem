package com.shortlink.admin.service;

import com.shortlink.admin.pojo.dto.ShortLinkCreateDTO;
import com.shortlink.admin.pojo.dto.ShortLinkPageDTO;
import com.shortlink.admin.pojo.dto.ShortLinkUpdateDTO;
import com.shortlink.admin.pojo.vo.ShortLinkCreateVO;
import com.shortlink.admin.pojo.vo.ShortLinkVO;
import com.shortlink.common.result.PageResult;

/**
 * 短链管理服务
 */
public interface ShortLinkService {

    /** 创建短链：生成唯一短码 + 写库 + 通知跳转服务布隆过滤器 */
    ShortLinkCreateVO createShortLink(ShortLinkCreateDTO reqDTO);

    /** 更新短链：变更原始链接/有效期 + 通知跳转服务清缓存 */
    void updateShortLink(ShortLinkUpdateDTO reqDTO);

    /** 修改短链状态（启用/禁用）+ 通知跳转服务清缓存 */
    void updateStatus(String shortCode, Integer status);

    /** 删除短链 + 通知跳转服务清缓存 */
    void deleteShortLink(String shortCode);

    /** 短码查详情 */
    ShortLinkVO getShortLink(String shortCode);

    /** 分页条件查询 */
    PageResult<ShortLinkVO> pageShortLink(ShortLinkPageDTO reqDTO);
}
