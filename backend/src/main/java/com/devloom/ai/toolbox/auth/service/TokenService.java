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
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthProperties authProperties;
    private final Clock clock;

    @Transactional
    public TokenResponse issueTokenPair(UserEntity user) {
        String normalizedDevice = normalizeDevice(DeviceContextHolder.getDeviceId());
        refreshTokenRepository.deleteByUserAndDevice(user, normalizedDevice);
        String refreshTokenValue = RandomUtil.randomHex(32);
        Instant now = clock.instant();
        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .user(user)
                .token(refreshTokenValue)
                .device(normalizedDevice)
                .expiresAt(now.plus(authProperties.getToken().getRefreshTokenTtlDays(), ChronoUnit.DAYS))
                .build();
        refreshTokenRepository.save(refreshToken);
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getNickname());
        return new TokenResponse(accessToken, refreshTokenValue, jwtTokenProvider.getAccessTokenTtlSeconds());
    }

    @Transactional
    public TokenResponse refresh(String refreshTokenValue) {
        Instant now = clock.instant();
        RefreshTokenEntity refreshToken = refreshTokenRepository
                .findByTokenAndExpiresAtAfter(refreshTokenValue, now)
                .orElseThrow(() -> new BizException(BizErrorCode.REFRESH_TOKEN_INVALID));
        refreshTokenRepository.delete(refreshToken);
        return issueTokenPair(refreshToken.getUser(), refreshToken.getDevice());
    }

    @Transactional
    public void revoke(UserEntity user, LogoutScope scope) {
        if (scope == LogoutScope.ALL) {
            refreshTokenRepository.deleteByUser(user);
        } else {
            refreshTokenRepository.deleteByUserAndDevice(user, normalizeDevice(DeviceContextHolder.getDeviceId()));
        }
    }

    private String normalizeDevice(String device) {
        if (StringUtils.hasText(device)) {
            return device.trim();
        }
        return authProperties.getToken().getDefaultDevice();
    }
}
