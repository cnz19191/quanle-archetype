package com.isiniu.single.auth.controller;

import com.isiniu.common.api.CommonResult;
import com.isiniu.common.web.OperationLog;
import com.isiniu.single.auth.dto.request.LoginRequest;
import com.isiniu.single.auth.dto.request.RefreshTokenRequest;
import com.isiniu.single.auth.dto.response.LoginUserResponse;
import com.isiniu.single.auth.dto.response.TokenResponse;
import com.isiniu.single.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 鉴权接口。LOCAL 模式提供登录、刷新、登出；SSO 模式只使用当前用户接口。
 *
 * @author isiniu
 */
@Validated
@RestController
@RequestMapping("/api/auth")
@Tag(name = "鉴权")
public class AuthController {

    @Resource
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "账号密码登录")
    @OperationLog(module = "鉴权", action = "登录")
    public CommonResult<TokenResponse> login(@Validated @RequestBody LoginRequest request) {
        return CommonResult.buildSuccessResult(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌")
    @OperationLog(module = "鉴权", action = "刷新令牌")
    public CommonResult<TokenResponse> refresh(@Validated @RequestBody RefreshTokenRequest request) {
        return CommonResult.buildSuccessResult(authService.refresh(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "登出")
    @OperationLog(module = "鉴权", action = "登出")
    public CommonResult<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return CommonResult.buildSuccessResult();
    }

    @GetMapping("/me")
    @Operation(summary = "当前登录用户")
    public CommonResult<LoginUserResponse> me() {
        return CommonResult.buildSuccessResult(authService.currentUser());
    }
}
