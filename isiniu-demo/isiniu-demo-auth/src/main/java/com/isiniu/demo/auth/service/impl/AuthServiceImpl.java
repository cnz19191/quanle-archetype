package com.isiniu.demo.auth.service.impl;

import com.isiniu.common.enums.CommonErrorCode;
import com.isiniu.common.exception.CustomException;
import com.isiniu.common.security.AuthMode;
import com.isiniu.common.security.AuthProperties;
import com.isiniu.common.security.JwtTokenProvider;
import com.isiniu.common.security.LoginUser;
import com.isiniu.common.web.LoginUserHolder;
import com.isiniu.demo.auth.convert.AuthConvert;
import com.isiniu.demo.auth.dto.request.LoginRequest;
import com.isiniu.demo.auth.dto.request.RefreshTokenRequest;
import com.isiniu.demo.auth.dto.response.LoginUserResponse;
import com.isiniu.demo.auth.dto.response.TokenResponse;
import com.isiniu.demo.auth.exception.ErrorCodeEnum;
import com.isiniu.demo.auth.model.RefreshTokenModel;
import com.isiniu.demo.auth.model.UserModel;
import com.isiniu.demo.auth.repository.RefreshTokenRepository;
import com.isiniu.demo.auth.repository.UserRepository;
import com.isiniu.demo.auth.service.AuthService;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 内置账号密码鉴权。refresh token 只存 SHA-256 哈希，刷新时轮换。
 *
 * @author isiniu
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private UserRepository userRepository;

    @Resource
    private RefreshTokenRepository refreshTokenRepository;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private JwtTokenProvider jwtTokenProvider;

    @Resource
    private AuthProperties authProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TokenResponse login(LoginRequest request) {
        requireLocalMode();
        UserModel user = userRepository.getByUsername(request.getUsername());
        if (user == null
                || !StringUtils.hasText(user.getPasswordHash())
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("[登录失败] username={}", request.getUsername());
            throw new CustomException(ErrorCodeEnum.AUTH_BAD_CREDENTIALS, "username=" + request.getUsername());
        }
        if (user.getStatus() == null || user.getStatus() != UserModel.STATUS_ENABLED) {
            log.warn("[登录失败-账号停用] username={}, userId={}", request.getUsername(), user.getId());
            throw new CustomException(ErrorCodeEnum.AUTH_USER_DISABLED, "username=" + request.getUsername());
        }
        log.info("[登录成功] userId={}, username={}", user.getId(), user.getUsername());
        return issueTokens(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TokenResponse refresh(RefreshTokenRequest request) {
        requireLocalMode();
        String tokenHash = jwtTokenProvider.hashRefreshToken(request.getRefreshToken());
        RefreshTokenModel record = refreshTokenRepository.getByTokenHash(tokenHash);
        if (record == null || record.getRevoked() == null || record.getRevoked() == RefreshTokenModel.REVOKED) {
            log.warn("[刷新令牌失败] reason=invalid");
            throw new CustomException(ErrorCodeEnum.AUTH_REFRESH_INVALID, "refreshToken 无效");
        }
        if (record.getExpiresAt() == null || record.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("[刷新令牌失败] reason=expired, userId={}", record.getUserId());
            throw new CustomException(ErrorCodeEnum.AUTH_REFRESH_EXPIRED, "refreshToken 已过期");
        }
        UserModel user = userRepository.getById(record.getUserId());
        if (user == null) {
            throw new CustomException(ErrorCodeEnum.AUTH_USER_NOT_FOUND, "userId=" + record.getUserId());
        }
        if (user.getStatus() == null || user.getStatus() != UserModel.STATUS_ENABLED) {
            throw new CustomException(ErrorCodeEnum.AUTH_USER_DISABLED, "userId=" + record.getUserId());
        }
        if (!refreshTokenRepository.revokeIfActive(tokenHash)) {
            log.warn("[刷新令牌失败] reason=concurrentReuse, userId={}", record.getUserId());
            throw new CustomException(ErrorCodeEnum.AUTH_REFRESH_INVALID, "refreshToken 无效");
        }
        log.info("[刷新令牌] userId={}", user.getId());
        return issueTokens(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(String refreshToken) {
        requireLocalMode();
        RefreshTokenModel record =
                refreshTokenRepository.getByTokenHash(jwtTokenProvider.hashRefreshToken(refreshToken));
        if (record == null || record.getRevoked() == null || record.getRevoked() == RefreshTokenModel.REVOKED) {
            return;
        }
        Long currentUserId = LoginUserHolder.getUserId();
        if (currentUserId == null || !currentUserId.equals(record.getUserId())) {
            log.warn("[登出忽略] reason=tokenNotOwned, userId={}", currentUserId);
            return;
        }
        refreshTokenRepository.revokeIfActive(record.getTokenHash());
        log.info("[登出] userId={}", record.getUserId());
    }

    @Override
    public LoginUserResponse currentUser() {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            throw new CustomException(CommonErrorCode.UNAUTHORIZED, "未登录");
        }
        UserModel user = userRepository.getById(userId);
        if (user == null) {
            throw new CustomException(ErrorCodeEnum.AUTH_USER_NOT_FOUND, "userId=" + userId);
        }
        return AuthConvert.INSTANCE.toLoginUserResponse(user);
    }

    private void requireLocalMode() {
        if (authProperties.getMode() != AuthMode.LOCAL) {
            throw new CustomException(ErrorCodeEnum.AUTH_MODE_NOT_SUPPORTED, "mode=" + authProperties.getMode());
        }
    }

    private TokenResponse issueTokens(UserModel user) {
        LoginUser loginUser = AuthConvert.INSTANCE.toLoginUser(user);
        String accessToken = jwtTokenProvider.createAccessToken(loginUser);
        String refreshToken = jwtTokenProvider.newRefreshToken();
        RefreshTokenModel record = new RefreshTokenModel();
        record.setUserId(user.getId());
        record.setTokenHash(jwtTokenProvider.hashRefreshToken(refreshToken));
        record.setExpiresAt(LocalDateTime.now().plus(authProperties.getRefreshTokenTtl()));
        record.setRevoked(RefreshTokenModel.NOT_REVOKED);
        refreshTokenRepository.save(record);
        TokenResponse response = new TokenResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtTokenProvider.accessTokenTtlSeconds());
        return response;
    }
}
