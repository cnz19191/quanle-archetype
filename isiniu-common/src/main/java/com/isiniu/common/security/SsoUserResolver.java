package com.isiniu.common.security;

import java.util.Map;

/**
 * SSO 模式下把外部身份映射到本地用户。实现放在 auth 领域，common 不依赖数据库。
 *
 * @author isiniu
 */
public interface SsoUserResolver {

    /**
     * 按外部主体查找本地用户。
     *
     * @param subject 外部主体，取自配置的 claim
     * @param claims 全部 claims
     * @return 本地用户，未绑定时返回 null
     */
    LoginUser resolve(String subject, Map<String, Object> claims);

    /**
     * 自动建号，仅在 {@code isiniu.auth.sso.auto-provision=true} 时调用。
     *
     * @param subject 外部主体
     * @param claims 全部 claims
     * @return 新建的本地用户，未实现时返回 null
     */
    default LoginUser provision(String subject, Map<String, Object> claims) {
        return null;
    }
}
