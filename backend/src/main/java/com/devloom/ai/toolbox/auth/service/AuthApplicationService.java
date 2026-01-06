package com.devloom.ai.toolbox.auth.service;

import com.devloom.ai.toolbox.auth.domain.entity.LoginAuditEntity;
import com.devloom.ai.toolbox.auth.domain.entity.UserAuthEntity;
import com.devloom.ai.toolbox.auth.domain.entity.UserEntity;
import com.devloom.ai.toolbox.auth.domain.enums.IdentityType;
import com.devloom.ai.toolbox.auth.domain.enums.LoginStatus;
import com.devloom.ai.toolbox.auth.domain.enums.LogoutScope;
import com.devloom.ai.toolbox.auth.domain.enums.UserStatus;
import com.devloom.ai.toolbox.auth.domain.enums.VerificationScene;
import com.devloom.ai.toolbox.auth.domain.repository.LoginAuditRepository;
import com.devloom.ai.toolbox.auth.domain.repository.UserAuthRepository;
import com.devloom.ai.toolbox.auth.domain.repository.UserRepository;
import com.devloom.ai.toolbox.auth.dto.request.EmailCodeRequest;
import com.devloom.ai.toolbox.auth.dto.request.LoginRequest;
import com.devloom.ai.toolbox.auth.dto.request.LogoutRequest;
import com.devloom.ai.toolbox.auth.dto.request.PasswordResetRequest;
import com.devloom.ai.toolbox.auth.dto.request.RefreshTokenRequest;
import com.devloom.ai.toolbox.auth.dto.request.RegisterRequest;
import com.devloom.ai.toolbox.auth.dto.response.ProfileResponse;
import com.devloom.ai.toolbox.auth.dto.response.ProfileResponse.AuthBindingResponse;
import com.devloom.ai.toolbox.auth.dto.response.RegisterResponse;
import com.devloom.ai.toolbox.auth.dto.response.TokenResponse;
import com.devloom.ai.toolbox.common.exception.BizErrorCode;
import com.devloom.ai.toolbox.common.exception.BizException;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthApplicationService {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final VerificationCodeService verificationCodeService;
    private final TokenService tokenService;
    private final LoginAuditRepository loginAuditRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userAuthRepository.existsByIdentityTypeAndIdentifier(IdentityType.EMAIL, email)) {
            throw new BizException(BizErrorCode.EMAIL_EXISTS);
        }
        verificationCodeService.verifyAndConsume(email, VerificationScene.REGISTER, request.getCode());
        UserEntity user = UserEntity.builder()
                .nickname(request.getNickname())
                .avatar("")
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);
        UserAuthEntity auth = UserAuthEntity.builder()
                .user(user)
                .identityType(IdentityType.EMAIL)
                .identifier(email)
                .credential(passwordEncoder.encode(request.getPassword()))
                .verified(true)
                .build();
        userAuthRepository.save(auth);
        TokenResponse token = tokenService.issueTokenPair(user, request.getDevice());
        return RegisterResponse.builder().userId(user.getId()).token(token).build();
    }

    @Transactional
    public TokenResponse login(LoginRequest request, String ip, String userAgent) {
        IdentityType identityType = request.getType() == null ? IdentityType.EMAIL : request.getType();
        String identifier = normalizeIdentifier(identityType, request.getIdentifier());
        UserAuthEntity auth = userAuthRepository
                .findByIdentityTypeAndIdentifier(identityType, identifier)
                .orElseThrow(() -> new BizException(BizErrorCode.UNAUTHORIZED));
        UserEntity user = auth.getUser();
        if (user.getStatus() == UserStatus.LOCKED) {
            recordLogin(user, identityType, identifier, ip, userAgent, LoginStatus.FAILURE);
            throw new BizException(BizErrorCode.ACCOUNT_LOCKED);
        }
        if (user.getStatus() == UserStatus.DELETED) {
            recordLogin(user, identityType, identifier, ip, userAgent, LoginStatus.FAILURE);
            throw new BizException(BizErrorCode.UNAUTHORIZED);
        }
        if (!passwordEncoder.matches(request.getPassword(), auth.getCredential())) {
            recordLogin(user, identityType, identifier, ip, userAgent, LoginStatus.FAILURE);
            throw new BizException(BizErrorCode.PASSWORD_MISMATCH);
        }
        auth.setLastLoginAt(clock.instant());
        userAuthRepository.save(auth);
        recordLogin(user, identityType, identifier, ip, userAgent, LoginStatus.SUCCESS);
        return tokenService.issueTokenPair(user, request.getDevice());
    }

    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        return tokenService.refresh(request.getRefreshToken());
    }

    @Transactional
    public void logout(Long userId, LogoutRequest request, String device) {
        UserEntity currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(BizErrorCode.RESOURCE_NOT_FOUND));
        LogoutScope scope = request.getScope() == null ? LogoutScope.CURRENT : request.getScope();
        tokenService.revoke(currentUser, scope, device);
    }

    @Transactional
    public void requestEmailCode(EmailCodeRequest request) {
        verificationCodeService.requestEmailCode(request);
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BizException(BizErrorCode.PASSWORD_MISMATCH);
        }
        String email = normalizeEmail(request.getEmail());
        verificationCodeService.verifyAndConsume(email, VerificationScene.RESET_PASSWORD, request.getCode());
        UserAuthEntity auth = userAuthRepository
                .findByIdentityTypeAndIdentifier(IdentityType.EMAIL, email)
                .orElseThrow(() -> new BizException(BizErrorCode.RESOURCE_NOT_FOUND));
        auth.setCredential(passwordEncoder.encode(request.getNewPassword()));
        userAuthRepository.save(auth);
        tokenService.revoke(auth.getUser(), LogoutScope.ALL, null);
    }

    @Transactional(readOnly = true)
    public ProfileResponse profile(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(BizErrorCode.RESOURCE_NOT_FOUND));
        if (user.getStatus() == UserStatus.DELETED) {
            throw new BizException(BizErrorCode.RESOURCE_NOT_FOUND);
        }
        List<AuthBindingResponse> bindings = userAuthRepository.findByUser(user).stream()
                .map(auth -> AuthBindingResponse.builder()
                        .type(auth.getIdentityType())
                        .identifier(auth.getIdentifier())
                        .verified(auth.isVerified())
                        .build())
                .collect(Collectors.toList());
        return ProfileResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .bindings(bindings)
                .build();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeIdentifier(IdentityType identityType, String identifier) {
        if (identityType == IdentityType.EMAIL) {
            return normalizeEmail(identifier);
        }
        return identifier;
    }

    private void recordLogin(
            UserEntity user, IdentityType type, String identifier, String ip, String userAgent, LoginStatus status) {
        LoginAuditEntity audit = LoginAuditEntity.builder()
                .user(user)
                .identityType(type)
                .identifier(identifier)
                .ip(ip)
                .userAgent(StringUtils.hasText(userAgent) ? userAgent : "")
                .status(status)
                .build();
        loginAuditRepository.save(audit);
    }
}
