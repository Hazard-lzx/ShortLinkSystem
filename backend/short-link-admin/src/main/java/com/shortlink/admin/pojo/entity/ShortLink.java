package com.shortlink.admin.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 短链接
 */
@Data
@TableName("t_short_link")
public class ShortLink implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String shortCode;

    private String originalUrl;

    /** 过期时间，NULL 表示永久有效 */
    private LocalDateTime expireTime;

    /** 0-启用 1-禁用 */
    private Integer status;

    private Long visitCount;

    @TableField(insertStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(insertStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}
