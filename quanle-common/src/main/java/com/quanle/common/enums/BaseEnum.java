package com.quanle.common.enums;

/**
 * 业务枚举契约。每个枚举仍需自行提供 {@code getDesc(String code)} 和 {@code getByValue(Integer value)}。
 *
 * @author quanle
 */
public interface BaseEnum {

    /**
     * 枚举编码，对外和日志使用。
     *
     * @return 编码
     */
    String getCode();

    /**
     * 落库值。
     *
     * @return 数值
     */
    Integer getValue();

    /**
     * 中文说明。
     *
     * @return 说明
     */
    String getDesc();
}
