package com.isiniu.demo.auth.service;

import com.isiniu.demo.auth.dto.request.LoginRequest;
import com.isiniu.demo.auth.dto.request.RefreshTokenRequest;
import com.isiniu.demo.auth.dto.response.LoginUserResponse;
import com.isiniu.demo.auth.dto.response.TokenResponse;

/**
 * 鉴权服务。LOCAL 模式提供登录、刷新、登出；SSO 模式只提供当前用户查询。
 *
 * @author isiniu
 */
public interface AuthService {

    /**
     * 账号密码登录，签发 access token 和 refresh token。
     *
     * @param request 登录请求
     * @return 令牌对
     */
    TokenResponse login(LoginRequest request);

    /**
     * 刷新令牌。旧 refresh token 立即作废，返回新的令牌对。
     *
     * @param request 刷新请求
     * @return 新的令牌对
     */
    TokenResponse refresh(RefreshTokenRequest request);

    /**
     * 吊销 refresh token。
     *
     * @param refreshToken 刷新令牌
     */
    void logout(String refreshToken);

    /**
     * 当前登录用户。
     *
     * @return 当前用户
     */
    LoginUserResponse currentUser();
}
