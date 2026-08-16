package com.shortlink.common.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.RandomUtil;

/**
 * 短码生成器：雪花 ID 转 62 进制
 *
 * <p>雪花 ID 单调递增且全局唯一，转换为 62 进制后长度约 11 位，
 * 从根本上避免随机短码的唯一索引冲突问题。
 */
public class ShortCodeGenerator {

    private static final String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final int RADIX = CHARS.length();

    private static final Snowflake SNOWFLAKE = new Snowflake(
            RandomUtil.randomLong(0, 31), RandomUtil.randomLong(0, 31));

    private ShortCodeGenerator() {
    }

    public static String generate() {
        return toBase62(SNOWFLAKE.nextId());
    }

    public static String toBase62(long id) {
        if (id == 0) {
            return String.valueOf(CHARS.charAt(0));
        }
        StringBuilder sb = new StringBuilder();
        long num = id;
        while (num > 0) {
            sb.append(CHARS.charAt((int) (num % RADIX)));
            num /= RADIX;
        }
        return sb.reverse().toString();
    }
}
