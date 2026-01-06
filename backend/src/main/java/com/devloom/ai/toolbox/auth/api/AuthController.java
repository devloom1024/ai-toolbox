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

    /**
     * POST /api/v1/auth/register
     * <p>邮箱+验证码注册账号，详情见 docs/design/auth/openapi.yml。</p>
     */
    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authApplicationService.register(request);
        return ApiResponse.success(response);
    }

    /**
     * POST /api/v1/auth/login
     * <p>邮箱和密码登录，返回 Access/Refresh Token。</p>
     */
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        String ip = RequestHeaderExtractor.resolveClientIp(httpServletRequest);
        String userAgent = RequestHeaderExtractor.resolveUserAgent(httpServletRequest);
        TokenResponse response = authApplicationService.login(request, ip, userAgent);
        return ApiResponse.success(response);
    }

    /**
     * POST /api/v1/auth/token/refresh
     * <p>使用 Refresh Token 刷新 Access Token。</p>
     */
    @PostMapping("/token/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authApplicationService.refreshToken(request));
    }

    /**
     * POST /api/v1/auth/logout
     * <p>注销当前设备或所有设备的登录态。</p>
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @Valid @RequestBody(required = false) LogoutRequest request, @AuthenticationPrincipal CurrentUser currentUser) {
        LogoutRequest payload = request == null ? new LogoutRequest() : request;
        authApplicationService.logout(currentUser.getUserId(), payload);
        return ApiResponse.success(null);
    }

    /**
     * POST /api/v1/auth/code/email
     * <p>发送邮箱验证码，支持注册和重置密码场景。</p>
     */
    @PostMapping("/code/email")
    public ApiResponse<Void> sendEmailCode(@Valid @RequestBody EmailCodeRequest request) {
        authApplicationService.requestEmailCode(request);
        return ApiResponse.success(null);
    }

    /**
     * POST /api/v1/auth/password/reset
     * <p>根据邮箱验证码重置登录密码。</p>
     */
    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authApplicationService.resetPassword(request);
        return ApiResponse.success(null);
    }

    /**
     * GET /api/v1/auth/profile
     * <p>查询当前登录用户资料与绑定信息。</p>
     */
    @GetMapping("/profile")
    public ApiResponse<ProfileResponse> profile(@AuthenticationPrincipal CurrentUser currentUser) {
        ProfileResponse response = authApplicationService.profile(currentUser.getUserId());
        return ApiResponse.success(response);
    }

    /**
     * GET /api/v1/auth/oauth/linuxdo/authorize
     * <p>生成 LinuxDo OAuth 授权链接以及 state。</p>
     */
    @GetMapping("/oauth/linuxdo/authorize")
    public ApiResponse<LinuxDoAuthorizeResponse> linuxDoAuthorize(
            @RequestParam(value = "redirect_uri", required = false) String redirectUri) {
        LinuxDoAuthorizeResponse response = linuxDoOAuthService.authorize(redirectUri);
        return ApiResponse.success(response);
    }

    /**
     * GET /api/v1/auth/oauth/linuxdo/callback
     * <p>处理 LinuxDo OAuth 回调，返回登录后的 Token。</p>
     */
    @GetMapping("/oauth/linuxdo/callback")
    public ApiResponse<TokenResponse> linuxDoCallback(
            @RequestParam("code") @NotBlank(message = "{validation.oauth.code.required}") String code,
            @RequestParam("state") @NotBlank(message = "{validation.oauth.state.required}") String state) {
        TokenResponse response = linuxDoOAuthService.handleCallback(code, state);
        return ApiResponse.success(response);
    }

    /**
     * POST /api/v1/auth/bind/linuxdo
     * <p>将当前账号与 LinuxDo 账号绑定。</p>
     */
    @PostMapping("/bind/linuxdo")
    public ApiResponse<Void> bindLinuxDo(
            @AuthenticationPrincipal CurrentUser currentUser, @Valid @RequestBody BindLinuxDoRequest request) {
        linuxDoOAuthService.bind(currentUser.getUserId(), request);
        return ApiResponse.success(null);
    }

}
