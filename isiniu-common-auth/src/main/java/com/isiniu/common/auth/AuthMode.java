package com.isiniu.common.auth;

/**
 * 鉴权模式。
 *
 * @author isiniu
 */
public enum AuthMode {

    /** 内置账号密码登录，签发 JWT。 */
    LOCAL,

    /** OAuth2 Resource Server，校验外部 OIDC 的 JWT。 */
    SSO
}
