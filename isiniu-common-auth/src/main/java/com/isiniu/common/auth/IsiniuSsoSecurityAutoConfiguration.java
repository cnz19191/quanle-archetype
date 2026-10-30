package com.isiniu.common.auth;

import com.isiniu.common.enums.CommonErrorCode;
import com.isiniu.common.security.SsoUserResolver;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

/**
 * SSO 模式：作为 OAuth2 Resource Server 校验 OIDC JWT。
 *
 * @author isiniu
 */
@AutoConfiguration(before = ServletWebSecurityAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({SecurityFilterChain.class, JwtDecoder.class})
@ConditionalOnProperty(prefix = "isiniu.auth", name = "mode", havingValue = "sso")
@EnableConfigurationProperties(AuthProperties.class)
public class IsiniuSsoSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SecurityErrorWriter securityErrorWriter(JsonMapper jsonMapper) {
        return new SecurityErrorWriter(jsonMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder(AuthProperties properties) {
        AuthProperties.Sso sso = properties.getSso();
        if (!StringUtils.hasText(sso.getIssuerUri())) {
            throw new IllegalStateException("SSO 模式需要配置 isiniu.auth.sso.issuer-uri，用于校验令牌的 iss");
        }
        String issuer = sso.getIssuerUri().trim();
        List<String> audiences = sso.getAudiences() == null
                ? List.of()
                : sso.getAudiences().stream()
                        .filter(StringUtils::hasText)
                        .map(String::trim)
                        .toList();
        if (audiences.isEmpty()) {
            throw new IllegalStateException("SSO 模式需要配置 isiniu.auth.sso.audiences，用于校验令牌的 aud");
        }
        NimbusJwtDecoder decoder = StringUtils.hasText(sso.getJwkSetUri())
                ? NimbusJwtDecoder.withJwkSetUri(sso.getJwkSetUri().trim()).build()
                : NimbusJwtDecoder.withIssuerLocation(issuer).build();
        decoder.setJwtValidator(jwtValidator(issuer, audiences));
        return decoder;
    }

    /**
     * 校验有效期、iss 和 aud。
     *
     * @param issuer 期望的签发者
     * @param audiences 本应用接受的 aud
     * @return 组合校验器
     */
    static OAuth2TokenValidator<Jwt> jwtValidator(String issuer, List<String> audiences) {
        OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<List<String>>(
                JwtClaimNames.AUD, aud -> aud != null && aud.stream().anyMatch(audiences::contains));
        return new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(issuer), audienceValidator);
    }

    @Bean
    @ConditionalOnMissingBean
    public SsoAuthenticationConverter ssoAuthenticationConverter(
            ObjectProvider<SsoUserResolver> resolverProvider, AuthProperties properties) {
        SsoUserResolver resolver = resolverProvider.getIfAvailable();
        if (resolver == null) {
            throw new IllegalStateException("SSO 模式需要提供 SsoUserResolver 实现，通常由 auth 模块提供");
        }
        return new SsoAuthenticationConverter(resolver, properties);
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain isiniuSecurityFilterChain(
            HttpSecurity http,
            AuthProperties properties,
            SsoAuthenticationConverter authenticationConverter,
            SecurityErrorWriter errorWriter)
            throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()
                        .requestMatchers(properties.getPermitPaths().toArray(String[]::new))
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .exceptionHandling(
                        handling -> handling.authenticationEntryPoint((request, response, ex) -> errorWriter.write(
                                        response, HttpStatus.UNAUTHORIZED, CommonErrorCode.UNAUTHORIZED, "未登录或登录已失效"))
                                .accessDeniedHandler((request, response, ex) -> errorWriter.write(
                                        response, HttpStatus.FORBIDDEN, CommonErrorCode.FORBIDDEN, "无权限访问")))
                .oauth2ResourceServer(
                        oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter)))
                .addFilterBefore(new LoginUserContextFilter(), AuthorizationFilter.class);
        return http.build();
    }
}
