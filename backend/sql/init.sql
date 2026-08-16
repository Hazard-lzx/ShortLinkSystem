-- =====================================================
-- 分布式短链接系统（微服务版）数据库初始化脚本
-- MySQL 8.0，共用单库，不做分库分表
-- =====================================================

CREATE DATABASE IF NOT EXISTS short_link DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE short_link;

-- ----------------------------
-- 短链接主表
-- ----------------------------
DROP TABLE IF EXISTS t_short_link;
CREATE TABLE t_short_link (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    short_code   VARCHAR(16)     NOT NULL                COMMENT '短码',
    original_url VARCHAR(1024)   NOT NULL                COMMENT '原始链接',
    expire_time  DATETIME                 DEFAULT NULL    COMMENT '过期时间，NULL 表示永久有效',
    status       TINYINT         NOT NULL DEFAULT 0       COMMENT '状态：0-启用 1-禁用',
    visit_count  BIGINT UNSIGNED NOT NULL DEFAULT 0       COMMENT '累计访问次数（Kafka 消费端批量累加）',
    create_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_short_code (short_code),
    KEY idx_expire_status (expire_time, status)
) ENGINE = InnoDB COMMENT '短链接主表';

-- ----------------------------
-- 访问日志表（Kafka 消费端批量写入）
-- ----------------------------
DROP TABLE IF EXISTS t_short_link_log;
CREATE TABLE t_short_link_log (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    short_code VARCHAR(16)     NOT NULL                COMMENT '短码',
    ip         VARCHAR(64)              DEFAULT NULL   COMMENT '访问者 IP',
    user_agent VARCHAR(512)            DEFAULT NULL   COMMENT '访问者 UA',
    visit_time DATETIME        NOT NULL                COMMENT '访问时间',
    PRIMARY KEY (id),
    KEY idx_short_code (short_code),
    KEY idx_visit_time (visit_time)
) ENGINE = InnoDB COMMENT '短链接访问日志表';

-- ----------------------------
-- 用户表（默认账号在 short-link-admin 启动时自动初始化：admin / 123456）
-- ----------------------------
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(64)     NOT NULL                COMMENT '用户名',
    password    VARCHAR(128)   NOT NULL                COMMENT '密码（BCrypt 加密）',
    phone       VARCHAR(20)              DEFAULT NULL   COMMENT '手机号',
    status      TINYINT         NOT NULL DEFAULT 0      COMMENT '状态：0-正常 1-禁用',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB COMMENT '用户表';
