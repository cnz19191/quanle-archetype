package com.isiniu.common.web;

import com.isiniu.common.security.LoginUser;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前请求的登录用户。只在请求线程内有效，异步任务要显式传递用户 ID。
 *
 * <p>业务代码只依赖本类，不直接依赖 Spring Security 的类型。
 *
 * @author isiniu
 */
public final class LoginUserHolder {

    private LoginUserHolder() {}

    /**
     * 当前登录用户，未登录时返回 null。
     *
     * @return 登录用户
     */
    public static LoginUser current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }

    /**
     * 当前用户 ID，未登录时返回 null。
     *
     * @return 用户 ID
     */
    public static Long getUserId() {
        LoginUser loginUser = current();
        return loginUser == null ? null : loginUser.userId();
    }

    /**
     * 当前登录名，未登录时返回 null。
     *
     * @return 登录名
     */
    public static String getUsername() {
        LoginUser loginUser = current();
        return loginUser == null ? null : loginUser.username();
    }

    /**
     * 是否已登录。
     *
     * @return 已登录返回 true
     */
    public static boolean isAuthenticated() {
        return current() != null;
    }

    /**
     * 设置当前用户。仅供测试和异步任务显式传递用户使用，业务代码不要调用。
     *
     * @param userId 用户 ID
     */
    public static void setUserId(Long userId) {
        if (userId == null) {
            clear();
            return;
        }
        LoginUser loginUser = new LoginUser(userId, null, List.of());
        SecurityContextHolder.getContext()
                .setAuthentication(UsernamePasswordAuthenticationToken.authenticated(loginUser, null, List.of()));
    }

    /**
     * 清理当前请求的登录信息。
     */
    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}
