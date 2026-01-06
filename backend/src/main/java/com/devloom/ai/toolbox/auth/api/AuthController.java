package com.devloom.ai.toolbox.auth.api;

import com.devloom.ai.toolbox.auth.dto.request.BindLinuxDoRequest;
import com.devloom.ai.toolbox.auth.dto.request.EmailCodeRequest;
import com.devloom.ai.toolbox.auth.dto.request.LoginRequest;
import com.devloom.ai.toolbox.auth.dto.request.LogoutRequest;
import com.devloom.ai.toolbox.auth.dto.request.PasswordResetRequest;
import com.devloom.ai.toolbox.auth.dto.request.RefreshTokenRequest;
import com.devloom.ai.toolbox.auth.dto.request.RegisterRequest;
import com.devloom.ai.toolbox.auth.dto.response.ProfileResponse;
import com.devloom.ai.toolbox.auth.dto.response.RegisterResponse;
import com.devloom.ai.toolbox.auth.dto.response.TokenResponse;
import com.devloom.ai.toolbox.auth.service.AuthApplicationService;
import com.devloom.ai.toolbox.auth.service.LinuxDoOAuthService;
import com.devloom.ai.toolbox.common.response.ApiResponse;
import com.devloom.ai.toolbox.common.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ApiResponse<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceHeader) {
        request.setDevice(resolveDevice(request.getDevice(), deviceHeader));
        RegisterResponse response = authApplicationService.register(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceHeader,
            HttpServletRequest httpServletRequest) {
        request.setDevice(resolveDevice(request.getDevice(), deviceHeader));
        String ip = resolveClientIp(httpServletRequest);
        String userAgent = httpServletRequest.getHeader("User-Agent");
        TokenResponse response = authApplicationService.login(request, ip, userAgent);
        return ApiResponse.success(response);
    }

    @PostMapping("/token/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authApplicationService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @Valid @RequestBody(required = false) LogoutRequest request,
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceHeader) {
        LogoutRequest payload = request == null ? new LogoutRequest() : request;
        authApplicationService.logout(currentUser.getUserId(), payload, resolveDevice(null, deviceHeader));
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
    public ResponseEntity<Void> linuxDoAuthorize(@RequestParam(value = "redirect_uri", required = false) String redirectUri) {
        return linuxDoOAuthService.authorize(redirectUri);
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
        return ApiResponse.success(null);
    }

    private String resolveDevice(String fromPayload, String headerValue) {
        if (StringUtils.hasText(fromPayload)) {
            return fromPayload;
        }
        return headerValue;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
