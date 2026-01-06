package com.devloom.ai.toolbox.auth.api;

import com.devloom.ai.toolbox.auth.dto.request.*;
import com.devloom.ai.toolbox.auth.dto.response.LinuxDoAuthorizeResponse;
import com.devloom.ai.toolbox.auth.dto.response.ProfileResponse;
import com.devloom.ai.toolbox.auth.dto.response.RegisterResponse;
import com.devloom.ai.toolbox.auth.dto.response.TokenResponse;
import com.devloom.ai.toolbox.auth.service.AuthApplicationService;
import com.devloom.ai.toolbox.auth.service.LinuxDoOAuthService;
import com.devloom.ai.toolbox.common.response.ApiResponse;
import com.devloom.ai.toolbox.common.security.CurrentUser;
import com.devloom.ai.toolbox.common.web.RequestHeaderExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

    private final AuthApplicationService authApplicationService;
    private final LinuxDoOAuthService linuxDoOAuthService;

    public AuthController(AuthApplicationService authApplicationService, LinuxDoOAuthService linuxDoOAuthService) {
        this.authApplicationService = authApplicationService;
        this.linuxDoOAuthService = linuxDoOAuthService;
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authApplicationService.register(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        String ip = RequestHeaderExtractor.resolveClientIp(httpServletRequest);
        String userAgent = RequestHeaderExtractor.resolveUserAgent(httpServletRequest);
        TokenResponse response = authApplicationService.login(request, ip, userAgent);
        return ApiResponse.success(response);
    }

    @PostMapping("/token/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authApplicationService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @Valid @RequestBody(required = false) LogoutRequest request, @AuthenticationPrincipal CurrentUser currentUser) {
        LogoutRequest payload = request == null ? new LogoutRequest() : request;
        authApplicationService.logout(currentUser.getUserId(), payload);
        return ApiResponse.success(null);
    }

    @PostMapping("/code/email")
    public ApiResponse<Void> sendEmailCode(@Valid @RequestBody EmailCodeRequest request) {
        authApplicationService.requestEmailCode(request);
        return ApiResponse.success(null);
    }

    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authApplicationService.resetPassword(request);
        return ApiResponse.success(null);
    }

    @GetMapping("/profile")
    public ApiResponse<ProfileResponse> profile(@AuthenticationPrincipal CurrentUser currentUser) {
        ProfileResponse response = authApplicationService.profile(currentUser.getUserId());
        return ApiResponse.success(response);
    }

    @GetMapping("/oauth/linuxdo/authorize")
    public ApiResponse<LinuxDoAuthorizeResponse> linuxDoAuthorize(
            @RequestParam(value = "redirect_uri", required = false) String redirectUri) {
        LinuxDoAuthorizeResponse response = linuxDoOAuthService.authorize(redirectUri);
        return ApiResponse.success(response);
    }

    @GetMapping("/oauth/linuxdo/callback")
    public ApiResponse<TokenResponse> linuxDoCallback(
            @RequestParam("code") @NotBlank String code, @RequestParam("state") @NotBlank String state) {
        TokenResponse response = linuxDoOAuthService.handleCallback(code, state);
        return ApiResponse.success(response);
    }

    @PostMapping("/bind/linuxdo")
    public ApiResponse<Void> bindLinuxDo(
            @AuthenticationPrincipal CurrentUser currentUser, @Valid @RequestBody BindLinuxDoRequest request) {
        linuxDoOAuthService.bind(currentUser.getUserId(), request);
        return ApiResponse.success();
    }

}
