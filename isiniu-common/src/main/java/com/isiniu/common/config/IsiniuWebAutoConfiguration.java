package com.isiniu.common.config;

import com.isiniu.common.exception.GlobalExceptionHandler;
import com.isiniu.common.web.ApiLogAspect;
import com.isiniu.common.web.MdcTaskDecorator;
import com.isiniu.common.web.OperationLogAspect;
import com.isiniu.common.web.TraceIdFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * Web 公共自动配置：traceId、接口日志、操作日志、全局异常。
 * 鉴权和跨域由 security / cors 自动配置提供，业务项目只写配置。
 *
 * @author isiniu
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class IsiniuWebAutoConfiguration {

    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilterRegistration() {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceIdFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setName("traceIdFilter");
        return registration;
    }

    @Bean
    public ApiLogAspect apiLogAspect() {
        return new ApiLogAspect();
    }

    @Bean
    public OperationLogAspect operationLogAspect() {
        return new OperationLogAspect();
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    public MdcTaskDecorator mdcTaskDecorator() {
        return new MdcTaskDecorator();
    }
}
