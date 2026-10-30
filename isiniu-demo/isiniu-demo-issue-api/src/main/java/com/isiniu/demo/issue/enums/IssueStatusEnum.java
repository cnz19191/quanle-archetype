package com.isiniu.demo.issue.enums;

import com.isiniu.common.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 问题状态。只允许待处理到处理中，再到已关闭。
 *
 * @author isiniu
 */
@Getter
@RequiredArgsConstructor
public enum IssueStatusEnum implements BaseEnum {

    /** 待处理。 */
    PENDING("PENDING", 0, "待处理"),

    /** 处理中。 */
    PROCESSING("PROCESSING", 1, "处理中"),

    /** 已关闭。 */
    CLOSED("CLOSED", 2, "已关闭");

    private final String code;

    private final Integer value;

    private final String desc;

    /**
     * 按编码取中文说明。
     *
     * @param code 编码
     * @return 说明，不存在时返回 null
     */
    public static String getDesc(String code) {
        IssueStatusEnum item = getByCode(code);
        if (item == null) {
            return null;
        }
        return item.getDesc();
    }

    /**
     * 按落库值取枚举。
     *
     * @param value 落库值
     * @return 枚举，不存在时返回 null
     */
    public static IssueStatusEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (IssueStatusEnum item : values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 按编码取枚举。
     *
     * @param code 编码
     * @return 枚举，不存在时返回 null
     */
    public static IssueStatusEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (IssueStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static String codeOf(Integer value) {
        IssueStatusEnum item = getByValue(value);
        if (item == null) {
            return null;
        }
        return item.getCode();
    }

    public static String descOf(Integer value) {
        IssueStatusEnum item = getByValue(value);
        if (item == null) {
            return null;
        }
        return item.getDesc();
    }

    /**
     * 是否允许从当前状态迁到目标状态。
     *
     * @param target 目标状态
     * @return 允许时返回 true
     */
    public boolean canTransitTo(IssueStatusEnum target) {
        if (target == null) {
            return false;
        }
        return switch (this) {
            case PENDING -> target == PROCESSING;
            case PROCESSING -> target == CLOSED;
            case CLOSED -> false;
        };
    }
}
