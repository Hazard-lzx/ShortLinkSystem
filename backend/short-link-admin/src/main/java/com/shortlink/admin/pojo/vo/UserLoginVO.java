package com.shortlink.admin.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录结果
 */
@Data
public class UserLoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;

    private String username;

    /** Token 有效时长（小时） */
    private Long expireHour;
}
