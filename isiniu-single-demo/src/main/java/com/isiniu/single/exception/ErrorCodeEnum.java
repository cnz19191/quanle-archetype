package com.isiniu.single.exception;

import com.isiniu.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 问题领域错误码，序号落在 {@code ErrorCodeRange} 的 0200-0299。
 *
 * @author isiniu
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCodeEnum implements ErrorCode {

    /** 问题不存在，或对当前用户不可见。 */
    ISSUE_NOT_FOUND("ISN0201", "问题不存在"),

    /** 状态迁移不合法。 */
    ISSUE_STATUS_ILLEGAL("ISN0202", "问题状态不允许该操作"),

    /** 并发更新时状态已被改掉。 */
    ISSUE_STATUS_CONFLICT("ISN0203", "问题状态已变化，请刷新后重试"),

    /** 幂等号冲突。 */
    ISSUE_DUPLICATE("ISN0204", "业务幂等号已存在且内容不一致");

    private final String code;

    private final String message;
}
