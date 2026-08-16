package com.shortlink.common.constant;

/**
 * 通用常量
 */
public interface CommonConstant {

    /** 短链状态：启用 */
    Integer STATUS_ENABLE = 0;

    /** 短链状态：禁用 */
    Integer STATUS_DISABLE = 1;

    /** 请求头：Authorization */
    String HEADER_AUTHORIZATION = "Authorization";

    /** Token 前缀 */
    String TOKEN_PREFIX = "Bearer ";

    /** 网关鉴权后向下游传递的用户名请求头 */
    String HEADER_USERNAME = "X-Username";

    /** 缓存空对象标记（防布隆误判导致的穿透） */
    String EMPTY_CACHE_MARK = "__EMPTY__";

    /** 默认管理员账号 */
    String DEFAULT_ADMIN_USERNAME = "admin";

    String DEFAULT_ADMIN_PASSWORD = "123456";
}
