package com.quanle.common.enums;

import com.quanle.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 框架公共错误码，前缀固定为 SYS，不占用业务项目的模块前缀号段。
 *
 * @author quanle
 */
@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    /** 参数校验失败。 */
    PARAM_INVALID("SYS0001", "参数校验失败"),

    /** 未预期的系统异常。 */
    SYSTEM_ERROR("SYS0002", "系统异常"),

    /** 未登录。 */
    UNAUTHORIZED("SYS0003", "未登录或登录已失效");

    private final String code;

    private final String message;
}
