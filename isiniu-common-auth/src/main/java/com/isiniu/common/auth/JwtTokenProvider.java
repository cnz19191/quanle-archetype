package com.isiniu.common.auth;

import com.isiniu.common.security.LoginUser;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.util.StringUtils;

/**
 * LOCAL 模式的 JWT 签发与解析。access token 用 HMAC-SHA256 签名，refresh token 只存哈希。
 *
 * @author isiniu
 */
public class JwtTokenProvider {

    private static final int MIN_SECRET_BYTES = 32;

    private static final int REFRESH_TOKEN_BYTES = 32;

    private final AuthProperties properties;

    private final JwtEncoder encoder;

    private final JwtDecoder decoder;

    private final SecureRandom secureRandom = new SecureRandom();

    public JwtTokenProvider(AuthProperties properties) {
        this.properties = properties;
        SecretKey secretKey = secretKey(properties.getJwtSecret());
        JWKSource<SecurityContext> jwkSource = new ImmutableSecret<>(secretKey);
        this.encoder = new NimbusJwtEncoder(jwkSource);
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(requireIssuer(properties.getIssuer())));
        this.decoder = jwtDecoder;
    }

    /**
     * 签发 access token。
     *
     * @param loginUser 登录用户
     * @return token 字符串
     */
    public String createAccessToken(LoginUser loginUser) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getIssuer())
                .subject(String.valueOf(loginUser.userId()))
                .issuedAt(now)
                .expiresAt(now.plus(properties.getAccessTokenTtl()))
                .claim("username", loginUser.username())
                .claim("roles", loginUser.roles())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    /**
     * 解析并校验 access token：签名、有效期和 iss。
     *
     * @param token token 字符串
     * @return 解析后的 JWT
     */
    public Jwt decodeAccessToken(String token) {
        return decoder.decode(token);
    }

    /**
     * access token 有效期，单位秒。
     *
     * @return 秒数
     */
    public long accessTokenTtlSeconds() {
        return properties.getAccessTokenTtl().toSeconds();
    }

    /**
     * 生成 refresh token 原始串，只返回给客户端一次。
     *
     * @return 原始串
     */
    public String newRefreshToken() {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * refresh token 的 SHA-256 哈希，落库只存哈希。
     *
     * @param raw 原始串
     * @return 十六进制哈希
     */
    public String hashRefreshToken(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("当前 JDK 不支持 SHA-256", ex);
        }
    }

    private static String requireIssuer(String issuer) {
        if (!StringUtils.hasText(issuer)) {
            throw new IllegalStateException("isiniu.auth.issuer 未配置，LOCAL 模式无法校验令牌签发者");
        }
        return issuer;
    }

    private SecretKey secretKey(String secret) {
        if (!StringUtils.hasText(secret) || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("isiniu.auth.jwt-secret 未配置或长度不足 32 字节，LOCAL 模式无法启动");
        }
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
