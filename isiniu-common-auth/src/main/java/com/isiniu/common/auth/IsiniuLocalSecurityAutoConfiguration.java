package com.isiniu.common.auth;

import com.isiniu.common.enums.CommonErrorCode;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.json.JsonMapper;

/**
 * LOCAL 模式：内置账号密码登录，签发 JWT。
 *
 * @author isiniu
 */
@AutoConfiguration(before = ServletWebSecurityAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({SecurityFilterChain.class, JwtDecoder.class})
@ConditionalOnProperty(prefix = "isiniu.auth", name = "mode", havingValue = "local", matchIfMissing = true)
@EnableConfigurationProperties(AuthProperties.class)
public class IsiniuLocalSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtTokenProvider jwtTokenProvider(AuthProperties properties) {
        return new JwtTokenProvider(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityErrorWriter securityErrorWriter(JsonMapper jsonMapper) {
        return new SecurityErrorWriter(jsonMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        return new JwtAuthenticationFilter(tokenProvider);
    }

    /**
     * 过滤器只挂在 Spring Security 链里。它是 {@code Filter} 类型的 Bean，不关掉的话 Spring Boot 还会把它注册到 Servlet 容器，每个请求解析两次。
     *
     * @param jwtAuthenticationFilter JWT 过滤器
     * @return 已禁用的注册
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(jwtAuthenticationFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain isiniuSecurityFilterChain(
            HttpSecurity http,
            AuthProperties properties,
            JwtAuthenticationFilter jwtAuthenticationFilter,
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
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new LoginUserContextFilter(), AuthorizationFilter.class);
        return http.build();
    }
}
