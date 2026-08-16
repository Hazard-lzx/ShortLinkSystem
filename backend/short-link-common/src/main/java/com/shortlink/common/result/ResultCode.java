package com.shortlink.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一状态码
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "成功"),

    BAD_REQUEST(400, "请求参数错误"),

    UNAUTHORIZED(401, "未登录或登录已过期"),

    FORBIDDEN(403, "无访问权限"),

    NOT_FOUND(404, "资源不存在"),

    METHOD_NOT_ALLOWED(405, "请求方式不支持"),

    INTERNAL_ERROR(500, "系统内部错误"),

    BIZ_ERROR(600, "业务处理失败");

    private final Integer code;

    private final String message;
}
