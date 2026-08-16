package com.shortlink.admin.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 短链创建结果
 */
@Data
public class ShortLinkCreateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String shortCode;
}
