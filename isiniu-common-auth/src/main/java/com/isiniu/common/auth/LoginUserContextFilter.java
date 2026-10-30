package com.isiniu.common.auth;

import com.isiniu.common.security.LoginUser;
import com.isiniu.common.web.LoginUserHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 认证完成后把 Spring Security 中的 {@link LoginUser} 写入 {@link LoginUserHolder}，请求结束时清理，
 * 让业务代码不依赖 Spring Security。放在安全过滤器链的授权过滤器之前。
 *
 * @author isiniu
 */
public class LoginUserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof LoginUser loginUser) {
            LoginUserHolder.set(loginUser);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            LoginUserHolder.clear();
        }
    }
}
