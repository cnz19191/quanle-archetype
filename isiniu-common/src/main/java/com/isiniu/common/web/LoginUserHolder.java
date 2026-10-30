package com.isiniu.common.web;

import com.isiniu.common.security.LoginUser;
import java.util.List;

/**
 * 当前请求的登录用户。只在请求线程内有效，异步任务要显式传递用户 ID。
 *
 * <p>本类不依赖 Spring Security：由 {@code isiniu-common-auth} 在认证完成后写入、请求结束时清理。
 * 业务代码只依赖本类和 {@link LoginUser}。
 *
 * @author isiniu
 */
public final class LoginUserHolder {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private LoginUserHolder() {}

    /**
     * 当前登录用户，未登录时返回 null。
     *
     * @return 登录用户
     */
    public static LoginUser current() {
        return HOLDER.get();
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
     * 设置当前用户。由鉴权模块在认证完成后调用；业务代码只在测试和异步任务显式传递用户时使用。
     *
     * @param loginUser 登录用户，传 null 等同于 {@link #clear()}
     */
    public static void set(LoginUser loginUser) {
        if (loginUser == null) {
            clear();
            return;
        }
        HOLDER.set(loginUser);
    }

    /**
     * 只按用户 ID 设置当前用户。仅供测试和异步任务显式传递用户使用，业务代码不要调用。
     *
     * @param userId 用户 ID
     */
    public static void setUserId(Long userId) {
        set(userId == null ? null : new LoginUser(userId, null, List.of()));
    }

    /**
     * 清理当前线程的登录信息。
     */
    public static void clear() {
        HOLDER.remove();
    }
}
