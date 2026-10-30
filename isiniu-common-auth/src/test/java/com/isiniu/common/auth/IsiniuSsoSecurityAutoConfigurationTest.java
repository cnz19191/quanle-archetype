package com.isiniu.common.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;

/**
 * SSO 模式的令牌校验：签名、iss、aud 缺一不可。
 *
 * @author isiniu
 */
class IsiniuSsoSecurityAutoConfigurationTest {

    private static final String ISSUER = "https://idp.example.com";

    private static final String AUDIENCE = "isiniu-app";

    private static RSAKey signingKey;

    private static HttpServer jwksServer;

    private static String jwkSetUri;

    private final IsiniuSsoSecurityAutoConfiguration configuration = new IsiniuSsoSecurityAutoConfiguration();

    @BeforeAll
    static void startJwksServer() throws Exception {
        signingKey = new RSAKeyGenerator(2048).keyID("test-key").generate();
        byte[] body = new JWKSet(signingKey.toPublicJWK()).toString().getBytes(StandardCharsets.UTF_8);
        jwksServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        jwksServer.createContext("/jwks", exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        jwksServer.start();
        jwkSetUri = "http://127.0.0.1:" + jwksServer.getAddress().getPort() + "/jwks";
    }

    @AfterAll
    static void stopJwksServer() {
        jwksServer.stop(0);
    }

    @Test
    void acceptsTokenWithExpectedIssuerAndAudience() throws Exception {
        Jwt jwt = decoder().decode(token(ISSUER, List.of("other-app", AUDIENCE)));

        assertEquals("user-1", jwt.getSubject());
    }

    @Test
    void rejectsTokenIssuedForAnotherAudience() throws Exception {
        String token = token(ISSUER, List.of("other-app"));

        assertThrows(JwtValidationException.class, () -> decoder().decode(token));
    }

    @Test
    void rejectsTokenWithoutAudience() throws Exception {
        String token = token(ISSUER, null);

        assertThrows(JwtValidationException.class, () -> decoder().decode(token));
    }

    @Test
    void rejectsTokenFromAnotherIssuer() throws Exception {
        String token = token("https://evil.example.com", List.of(AUDIENCE));

        assertThrows(JwtValidationException.class, () -> decoder().decode(token));
    }

    @Test
    void requiresIssuerUri() {
        AuthProperties properties = properties();
        properties.getSso().setIssuerUri(" ");

        assertThrows(IllegalStateException.class, () -> configuration.jwtDecoder(properties));
    }

    @Test
    void requiresAudiences() {
        AuthProperties properties = properties();
        properties.getSso().setAudiences(List.of(""));

        assertThrows(IllegalStateException.class, () -> configuration.jwtDecoder(properties));
    }

    private JwtDecoder decoder() {
        return configuration.jwtDecoder(properties());
    }

    private AuthProperties properties() {
        AuthProperties properties = new AuthProperties();
        properties.setMode(AuthMode.SSO);
        properties.getSso().setIssuerUri(ISSUER);
        properties.getSso().setJwkSetUri(jwkSetUri);
        properties.getSso().setAudiences(List.of(AUDIENCE));
        return properties;
    }

    private String token(String issuer, List<String> audiences) throws Exception {
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                .subject("user-1")
                .issuer(issuer)
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 60_000));
        if (audiences != null) {
            claims.audience(audiences);
        }
        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("test-key").build(), claims.build());
        jwt.sign(new RSASSASigner(signingKey));
        return jwt.serialize();
    }
}
