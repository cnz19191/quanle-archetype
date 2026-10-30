package com.isiniu.common.security;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 当前登录用户。业务代码只通过 {@link com.isiniu.common.web.LoginUserHolder} 读取，不依赖 Spring Security。
 *
 * @param userId 用户 ID
 * @param username 登录名
 * @param roles 角色
 * @author isiniu
 */
public record LoginUser(Long userId, String username, List<String> roles) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public LoginUser {
        roles = roles == null ? List.of() : List.copyOf(roles);
    }
}
