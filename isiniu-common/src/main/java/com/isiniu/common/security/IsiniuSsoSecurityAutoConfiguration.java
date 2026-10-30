package com.isiniu.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isiniu.common.config.IsiniuCorsAutoConfiguration;
import com.isiniu.common.enums.CommonErrorCode;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

/**
 * SSO 模式：作为 OAuth2 Resource Server 校验 OIDC JWT。
 *
 * @author isiniu
 */
@AutoConfiguration(before = SecurityAutoConfiguration.class, after = IsiniuCorsAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({SecurityFilterChain.class, JwtDecoder.class})
@ConditionalOnProperty(prefix = "isiniu.auth", name = "mode", havingValue = "sso")
@EnableConfigurationProperties(AuthProperties.class)
public class IsiniuSsoSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SecurityErrorWriter securityErrorWriter(ObjectMapper objectMapper) {
        return new SecurityErrorWriter(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder(AuthProperties properties) {
        AuthProperties.Sso sso = properties.getSso();
        if (StringUtils.hasText(sso.getJwkSetUri())) {
            return NimbusJwtDecoder.withJwkSetUri(sso.getJwkSetUri().trim()).build();
        }
        if (StringUtils.hasText(sso.getIssuerUri())) {
            return JwtDecoders.fromIssuerLocation(sso.getIssuerUri().trim());
        }
        throw new IllegalStateException("SSO 模式需要配置 isiniu.auth.sso.issuer-uri 或 jwk-set-uri");
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
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, ex) -> errorWriter.write(
                                response, HttpStatus.UNAUTHORIZED, CommonErrorCode.UNAUTHORIZED, "未登录或登录已失效"))
                        .accessDeniedHandler((request, response, ex) -> errorWriter.write(
                                response, HttpStatus.FORBIDDEN, CommonErrorCode.FORBIDDEN, "无权限访问")))
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter)));
        return http.build();
    }
}
