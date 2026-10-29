package com.quanle.single.config;

import com.quanle.common.web.AuthInterceptor;
import com.quanle.common.web.TraceIdConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册登录拦截器和跨域。全局异常、traceId、操作日志由 quanle-common 自动配置提供。
 *
 * @author quanle
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${quanle.web.cors-allowed-origins:*}")
    private String allowedOrigins;

    @Bean
    public AuthInterceptor authInterceptor() {
        return new AuthInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor()).addPathPatterns("/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        registry.addMapping("/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders(TraceIdConstants.HEADER)
                .allowCredentials(false)
                .maxAge(3600);
    }
}
