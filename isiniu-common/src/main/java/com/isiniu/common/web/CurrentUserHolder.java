package com.isiniu.common.web;

/**
 * 当前请求的登录用户。只在请求线程内有效，异步任务要显式传递用户 ID。
 *
 * @author isiniu
 */
public final class CurrentUserHolder {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    private CurrentUserHolder() {}

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void clear() {
        USER_ID.remove();
    }
}
