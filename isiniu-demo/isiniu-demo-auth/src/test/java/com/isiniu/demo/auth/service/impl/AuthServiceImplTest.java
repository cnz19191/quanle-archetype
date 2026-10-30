package com.isiniu.demo.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.isiniu.common.exception.CustomException;
import com.isiniu.common.security.AuthMode;
import com.isiniu.common.security.AuthProperties;
import com.isiniu.common.security.JwtTokenProvider;
import com.isiniu.demo.auth.dto.request.LoginRequest;
import com.isiniu.demo.auth.dto.request.RefreshTokenRequest;
import com.isiniu.demo.auth.dto.response.TokenResponse;
import com.isiniu.demo.auth.exception.ErrorCodeEnum;
import com.isiniu.demo.auth.model.RefreshTokenModel;
import com.isiniu.demo.auth.model.UserModel;
import com.isiniu.demo.auth.repository.RefreshTokenRepository;
import com.isiniu.demo.auth.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 登录与刷新令牌轮换。
 *
 * @author isiniu
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthProperties authProperties;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        lenient().when(authProperties.getMode()).thenReturn(AuthMode.LOCAL);
        lenient().when(authProperties.getRefreshTokenTtl()).thenReturn(Duration.ofDays(7));
    }

    @Test
    void loginIssuesTokens() {
        UserModel user = enabledUser();
        when(userRepository.getByUsername("tester")).thenReturn(user);
        when(passwordEncoder.matches("password", "hash")).thenReturn(true);
        when(jwtTokenProvider.createAccessToken(any())).thenReturn("access");
        when(jwtTokenProvider.newRefreshToken()).thenReturn("refresh");
        when(jwtTokenProvider.hashRefreshToken("refresh")).thenReturn("refresh-hash");
        when(jwtTokenProvider.accessTokenTtlSeconds()).thenReturn(1800L);

        TokenResponse response = authService.login(loginRequest("tester", "password"));

        assertEquals("access", response.getAccessToken());
        assertEquals("refresh", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(1800L, response.getExpiresIn());
        verify(refreshTokenRepository).save(any(RefreshTokenModel.class));
    }

    @Test
    void loginRejectsBadPassword() {
        UserModel user = enabledUser();
        when(userRepository.getByUsername("tester")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        CustomException ex =
                assertThrows(CustomException.class, () -> authService.login(loginRequest("tester", "wrong")));

        assertEquals(
                ErrorCodeEnum.AUTH_BAD_CREDENTIALS.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void refreshRejectsRevokedToken() {
        RefreshTokenModel record = new RefreshTokenModel();
        record.setRevoked(1);
        when(jwtTokenProvider.hashRefreshToken(anyString())).thenReturn("refresh-hash");
        when(refreshTokenRepository.getByTokenHash("refresh-hash")).thenReturn(record);

        CustomException ex = assertThrows(CustomException.class, () -> authService.refresh(refreshRequest("refresh")));

        assertEquals(
                ErrorCodeEnum.AUTH_REFRESH_INVALID.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void refreshRotatesTokenAndRevokesOldOne() {
        RefreshTokenModel record = new RefreshTokenModel();
        record.setId(1L);
        record.setUserId(7L);
        record.setRevoked(0);
        record.setExpiresAt(LocalDateTime.now().plusDays(1));
        UserModel user = enabledUser();
        when(jwtTokenProvider.hashRefreshToken("refresh")).thenReturn("refresh-hash");
        when(refreshTokenRepository.getByTokenHash("refresh-hash")).thenReturn(record);
        when(userRepository.getById(7L)).thenReturn(user);
        when(jwtTokenProvider.createAccessToken(any())).thenReturn("new-access");
        when(jwtTokenProvider.newRefreshToken()).thenReturn("new-refresh");
        when(jwtTokenProvider.hashRefreshToken("new-refresh")).thenReturn("new-hash");
        when(jwtTokenProvider.accessTokenTtlSeconds()).thenReturn(1800L);

        TokenResponse response = authService.refresh(refreshRequest("refresh"));

        assertEquals("new-access", response.getAccessToken());
        assertEquals(1, record.getRevoked());
        verify(refreshTokenRepository).updateById(record);
        verify(refreshTokenRepository).save(any(RefreshTokenModel.class));
    }

    private UserModel enabledUser() {
        UserModel user = new UserModel();
        user.setId(7L);
        user.setUsername("tester");
        user.setPasswordHash("hash");
        user.setDisplayName("tester");
        user.setStatus(UserModel.STATUS_ENABLED);
        return user;
    }

    private LoginRequest loginRequest(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    private RefreshTokenRequest refreshRequest(String token) {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(token);
        return request;
    }
}
