package com.isiniu.demo.auth.exception;

import com.isiniu.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 鉴权领域错误码，序号落在 kernel {@code ErrorCodeRange} 的 0100-0199。
 *
 * @author isiniu
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCodeEnum implements ErrorCode {

    /** 用户名或密码错误。 */
    AUTH_BAD_CREDENTIALS("ISN0101", "用户名或密码错误"),

    /** 账号已停用。 */
    AUTH_USER_DISABLED("ISN0102", "账号已停用"),

    /** 刷新令牌无效。 */
    AUTH_REFRESH_INVALID("ISN0103", "刷新令牌无效"),

    /** 刷新令牌已过期。 */
    AUTH_REFRESH_EXPIRED("ISN0104", "刷新令牌已过期"),

    /** 用户不存在。 */
    AUTH_USER_NOT_FOUND("ISN0105", "用户不存在"),

    /** SSO 用户未绑定本地账号。 */
    AUTH_SSO_NOT_BOUND("ISN0106", "SSO 用户未绑定本地账号"),

    /** 当前鉴权模式不支持该操作。 */
    AUTH_MODE_NOT_SUPPORTED("ISN0107", "当前鉴权模式不支持该操作");

    private final String code;

    private final String message;
}
