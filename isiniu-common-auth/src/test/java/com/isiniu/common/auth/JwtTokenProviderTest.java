package com.isiniu.common.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.isiniu.common.security.LoginUser;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;

/**
 * LOCAL 模式 JWT 签发与解析。
 *
 * @author isiniu
 */
class JwtTokenProviderTest {

    private AuthProperties properties;

    @BeforeEach
    void setUp() {
        properties = new AuthProperties();
        properties.setJwtSecret("0123456789abcdef0123456789abcdef");
        properties.setIssuer("isiniu-test");
        properties.setAccessTokenTtl(Duration.ofMinutes(5));
    }

    @Test
    void issueAndDecodeAccessToken() {
        JwtTokenProvider provider = new JwtTokenProvider(properties);
        String token = provider.createAccessToken(new LoginUser(7L, "tester", List.of("ADMIN")));

        Jwt jwt = provider.decodeAccessToken(token);

        assertEquals("7", jwt.getSubject());
        assertEquals("isiniu-test", jwt.getClaimAsString("iss"));
        assertEquals("tester", jwt.getClaimAsString("username"));
        assertEquals(List.of("ADMIN"), jwt.getClaimAsStringList("roles"));
        assertTrue(provider.accessTokenTtlSeconds() > 0);
    }

    @Test
    void rejectsTokenFromOtherIssuerWithSameSecret() {
        AuthProperties other = new AuthProperties();
        other.setJwtSecret(properties.getJwtSecret());
        other.setIssuer("other-app");
        other.setAccessTokenTtl(Duration.ofMinutes(5));
        String foreignToken = new JwtTokenProvider(other).createAccessToken(new LoginUser(7L, "tester", List.of()));

        JwtTokenProvider provider = new JwtTokenProvider(properties);

        assertThrows(JwtException.class, () -> provider.decodeAccessToken(foreignToken));
    }

    @Test
    void rejectsBlankIssuer() {
        properties.setIssuer(" ");
        assertThrows(IllegalStateException.class, () -> new JwtTokenProvider(properties));
    }

    @Test
    void rejectsShortSecret() {
        properties.setJwtSecret("too-short");
        assertThrows(IllegalStateException.class, () -> new JwtTokenProvider(properties));
    }

    @Test
    void refreshTokenIsRandomAndHashedDeterministically() {
        JwtTokenProvider provider = new JwtTokenProvider(properties);
        String first = provider.newRefreshToken();
        String second = provider.newRefreshToken();

        assertNotEquals(first, second);
        assertEquals(provider.hashRefreshToken(first), provider.hashRefreshToken(first));
        assertNotEquals(provider.hashRefreshToken(first), first);
    }
}
