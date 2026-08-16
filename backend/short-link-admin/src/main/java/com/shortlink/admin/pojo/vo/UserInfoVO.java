package com.shortlink.admin.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息
 */
@Data
public class UserInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String phone;

    private Integer status;

    private String createTime;
}
