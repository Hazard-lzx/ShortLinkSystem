package com.shortlink.admin.pojo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;

/**
 * 短链分页查询请求
 */
@Data
public class ShortLinkPageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Min(value = 1, message = "页码最小为 1")
    private Long current = 1L;

    @Min(value = 1, message = "每页条数最小为 1")
    @Max(value = 100, message = "每页条数最大为 100")
    private Long size = 10L;

    /** 短码（精确） */
    private String shortCode;

    /** 原始链接（模糊） */
    private String originalUrl;

    /** 状态：0-启用 1-禁用 */
    private Integer status;
}
