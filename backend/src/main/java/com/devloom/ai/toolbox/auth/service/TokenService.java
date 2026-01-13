package com.devloom.ai.toolbox.auth.service;

import com.devloom.ai.toolbox.auth.domain.entity.RefreshTokenEntity;
import com.devloom.ai.toolbox.auth.domain.entity.UserEntity;
import com.devloom.ai.toolbox.auth.domain.enums.LogoutScope;
import com.devloom.ai.toolbox.auth.domain.repository.RefreshTokenRepository;
import com.devloom.ai.toolbox.auth.dto.response.TokenResponse;
import com.devloom.ai.toolbox.auth.service.support.AuthProperties;
import com.devloom.ai.toolbox.common.exception.BizErrorCode;
import com.devloom.ai.toolbox.common.exception.BizException;
import com.devloom.ai.toolbox.common.security.JwtTokenProvider;
import com.devloom.ai.toolbox.common.util.RandomUtil;
import com.devloom.ai.toolbox.common.web.DeviceContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthProperties authProperties;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public TokenResponse issueTokenPair(UserEntity user) {
        return createTokenPair(user);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public TokenResponse refresh(String refreshTokenValue) {
        Instant now = clock.instant();
        String requestDevice = normalizeDevice(DeviceContextHolder.getDeviceId());

        // 查找 token（通过 token 字段查找，实际存储的是哈希值）
        RefreshTokenEntity refreshToken = refreshTokenRepository
                .findByTokenAndExpiresAtAfter(refreshTokenValue, now)
                .orElseThrow(() -> new BizException(BizErrorCode.REFRESH_TOKEN_INVALID));

        // 验证 tokenHash 是否匹配（防止 token 替换攻击）
        String tokenHash = DigestUtils.md5DigestAsHex(refreshTokenValue.getBytes());
        if (!tokenHash.equals(refreshToken.getTokenHash())) {
            log.warn("Refresh token hash mismatch, possible token tampering: userId={}",
                    refreshToken.getUser().getId());
            throw new BizException(BizErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 验证请求设备与 token 绑定的设备是否一致（防止 token 盗用到其他设备）
        String tokenDevice = refreshToken.getDevice();
        if (!requestDevice.equals(tokenDevice)) {
            log.warn("Refresh token used from different device: expected={}, actual={}, userId={}",
                    tokenDevice, requestDevice, refreshToken.getUser().getId());
        }

        // 删除该用户该设备上的所有旧 token（包括当前这个）
        refreshTokenRepository.deleteByUserAndDevice(refreshToken.getUser(), tokenDevice);
        refreshTokenRepository.flush();

        return createTokenPair(refreshToken.getUser(), tokenDevice);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void revoke(UserEntity user, LogoutScope scope, String device) {
        if (scope == LogoutScope.ALL) {
            refreshTokenRepository.deleteByUser(user);
        } else {
            refreshTokenRepository.deleteByUserAndDevice(user, normalizeDevice(device));
        }
    }

    private TokenResponse createTokenPair(UserEntity user) {
        String normalizedDevice = normalizeDevice(DeviceContextHolder.getDeviceId());
        return createTokenPair(user, normalizedDevice);
    }

    private TokenResponse createTokenPair(UserEntity user, String device) {
        // 删除该用户的该设备上的旧 token
        refreshTokenRepository.deleteByUserAndDevice(user, device);
        refreshTokenRepository.flush();  // 强制立即执行 DELETE，避免 Hibernate 延迟执行导致唯一索引冲突
        String refreshTokenValue = RandomUtil.randomHex(32);
        String tokenHash = DigestUtils.md5DigestAsHex(refreshTokenValue.getBytes());
        Instant now = clock.instant();
        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .user(user)
                .token(refreshTokenValue)
                .tokenHash(tokenHash)
                .device(device)
                .expiresAt(now.plus(authProperties.getToken().getRefreshTokenTtlDays(), ChronoUnit.DAYS))
                .build();
        refreshTokenRepository.save(refreshToken);
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getNickname());
        return new TokenResponse(accessToken, refreshTokenValue, jwtTokenProvider.getAccessTokenTtlSeconds());
    }

    private String normalizeDevice(String device) {
        if (StringUtils.hasText(device)) {
            return device.trim();
        }
        return authProperties.getToken().getDefaultDevice();
    }
}
