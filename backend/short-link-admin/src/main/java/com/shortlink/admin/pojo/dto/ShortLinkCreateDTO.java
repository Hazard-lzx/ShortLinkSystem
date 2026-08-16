package com.shortlink.admin.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;

/**
 * 短链创建请求
 */
@Data
public class ShortLinkCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "原始链接不能为空")
    @URL(message = "原始链接格式不正确，需以 http(s):// 开头")
    @Size(max = 1024, message = "原始链接长度不能超过 1024")
    private String originalUrl;

    /** 过期时间 yyyy-MM-dd HH:mm:ss，为空表示永久有效 */
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$", message = "过期时间格式应为 yyyy-MM-dd HH:mm:ss")
    private String expireTime;
}
