package com.isiniu.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * SSO 模式下外部 JWT 到本地用户的映射。
 *
 * @author isiniu
 */
class SsoAuthenticationConverterTest {

    @Test
    void mapsBoundUser() {
        AuthProperties properties = new AuthProperties();
        SsoUserResolver resolver = (subject, claims) -> new LoginUser(7L, "tester", List.of("USER"));
        SsoAuthenticationConverter converter = new SsoAuthenticationConverter(resolver, properties);

        AbstractAuthenticationToken token = converter.convert(jwt("sub-1"));

        assertEquals("tester", ((LoginUser) token.getPrincipal()).username());
        assertEquals(1, token.getAuthorities().size());
    }

    @Test
    void rejectsUnboundUser() {
        AuthProperties properties = new AuthProperties();
        SsoUserResolver resolver = (subject, claims) -> null;
        SsoAuthenticationConverter converter = new SsoAuthenticationConverter(resolver, properties);

        assertThrows(BadCredentialsException.class, () -> converter.convert(jwt("sub-2")));
    }

    private Jwt jwt(String subject) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .claim("roles", List.of("USER"))
                .build();
    }
}
