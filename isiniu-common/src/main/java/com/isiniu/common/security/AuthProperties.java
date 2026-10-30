package com.isiniu.common.security;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 鉴权配置。两种模式通过 {@code isiniu.auth.mode} 切换。
 *
 * @author isiniu
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "isiniu.auth")
public class AuthProperties {

    /** 鉴权模式，默认内置账号密码。 */
    private AuthMode mode = AuthMode.LOCAL;

    /** JWT 签发者标识，LOCAL 模式使用。 */
    private String issuer = "isiniu";

    /** LOCAL 模式的 HMAC 密钥，至少 32 字节，从环境变量读取。 */
    private String jwtSecret;

    /** access token 有效期。 */
    private Duration accessTokenTtl = Duration.ofMinutes(30);

    /** refresh token 有效期。 */
    private Duration refreshTokenTtl = Duration.ofDays(7);

    /** 免登录路径，支持 Ant 风格。 */
    private List<String> permitPaths = new ArrayList<>(List.of(
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/health",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/actuator/health",
            "/error"));

    /** SSO 模式配置。 */
    private Sso sso = new Sso();

    /**
     * SSO 模式配置。{@code jwkSetUri} 优先于 {@code issuerUri}。
     */
    @Getter
    @Setter
    public static class Sso {

        /** OIDC issuer 地址，配置后自动发现 jwk set。 */
        private String issuerUri;

        /** 直接指定 JWK Set 地址，适合企业微信、钉钉等不自带发现端点的 IdP。 */
        private String jwkSetUri;

        /** 取哪个 claim 作为本地用户标识。 */
        private String userIdClaim = "sub";

        /** 角色 claim 名称。 */
        private String rolesClaim = "roles";

        /** 用户未绑定时是否自动建号。 */
        private boolean autoProvision = false;
    }
}
