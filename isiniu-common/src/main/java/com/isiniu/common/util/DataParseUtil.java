package com.isiniu.common.util;

import org.springframework.util.StringUtils;

/**
 * MapStruct 转换时使用的解析工具。
 *
 * @author isiniu
 */
public final class DataParseUtil {

    private DataParseUtil() {}

    /**
     * 去掉首尾空白，空白字符串返回 null。
     *
     * @param value 原始字符串
     * @return 整理后的字符串
     */
    public static String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    /**
     * 把 ID 转成字符串，避免前端大数精度丢失。
     *
     * @param id 主键
     * @return 字符串形式，入参为 null 时返回 null
     */
    public static String toIdString(Long id) {
        if (id == null) {
            return null;
        }
        return Long.toString(id);
    }
}
