package com.isiniu.demo.comment.exception;

import com.isiniu.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 评论领域错误码，序号落在 kernel {@code ErrorCodeRange} 的 0300-0399。
 *
 * @author isiniu
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCodeEnum implements ErrorCode {

    /** 幂等号冲突。 */
    COMMENT_DUPLICATE("ISN0301", "评论幂等号已存在且内容不一致"),

    /** 评论不存在。 */
    COMMENT_NOT_FOUND("ISN0302", "评论不存在");

    private final String code;

    private final String message;
}
