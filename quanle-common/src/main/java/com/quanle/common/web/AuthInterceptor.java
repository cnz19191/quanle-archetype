package com.quanle.common.web;

import com.quanle.common.enums.CommonErrorCode;
import com.quanle.common.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录占位拦截器。从 {@code X-User-Id} 读取用户，生产环境应替换为统一认证。
 *
 * @author quanle
 */
public class AuthInterceptor implements HandlerInterceptor {

    public static final String USER_HEADER = "X-User-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        if (isAnonymous(handlerMethod) || isOpenPath(request.getRequestURI())) {
            return true;
        }
        String raw = request.getHeader(USER_HEADER);
        if (!StringUtils.hasText(raw)) {
            throw new CustomException(CommonErrorCode.UNAUTHORIZED, "缺少请求头 X-User-Id");
        }
        try {
            long userId = Long.parseLong(raw);
            if (userId <= 0) {
                throw new NumberFormatException("non positive");
            }
            CurrentUserHolder.setUserId(userId);
            return true;
        } catch (NumberFormatException ex) {
            throw new CustomException(CommonErrorCode.UNAUTHORIZED, "X-User-Id 非法");
        }
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        CurrentUserHolder.clear();
    }

    private boolean isAnonymous(HandlerMethod handlerMethod) {
        return handlerMethod.hasMethodAnnotation(Anonymous.class)
                || handlerMethod.getBeanType().isAnnotationPresent(Anonymous.class);
    }

    private boolean isOpenPath(String uri) {
        return uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/webjars/")
                || uri.equals("/favicon.ico");
    }
}
