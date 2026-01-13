package com.devloom.ai.toolbox.auth.service;

import com.devloom.ai.toolbox.auth.domain.entity.VerificationCodeEntity;
import com.devloom.ai.toolbox.auth.domain.enums.VerificationChannel;
import com.devloom.ai.toolbox.auth.domain.enums.VerificationScene;
import com.devloom.ai.toolbox.auth.domain.repository.VerificationCodeRepository;
import com.devloom.ai.toolbox.auth.dto.request.EmailCodeRequest;
import com.devloom.ai.toolbox.auth.infra.mail.MailClient;
import com.devloom.ai.toolbox.auth.service.support.AuthProperties;
import com.devloom.ai.toolbox.common.exception.BizErrorCode;
import com.devloom.ai.toolbox.common.exception.BizException;
import com.devloom.ai.toolbox.common.util.RandomUtil;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final VerificationCodeRepository verificationCodeRepository;
    private final MailClient mailClient;
    private final AuthProperties authProperties;
    private final Clock clock;

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void requestEmailCode(EmailCodeRequest request) {
        Instant now = clock.instant();
        AuthProperties.VerificationCodeProperties props = authProperties.getVerificationCode();
        Instant threshold = now.minus(props.getRequestIntervalSeconds(), ChronoUnit.SECONDS);
        String email = normalize(request.getEmail());
        long count = verificationCodeRepository.countByIdentifierAndChannelAndSceneAndCreatedAtAfter(
                email, VerificationChannel.EMAIL, request.getScene(), threshold);
        if (count > 0) {
            throw new BizException(BizErrorCode.RATE_LIMIT);
        }
        String code = RandomUtil.numericCode(6);
        VerificationCodeEntity entity = VerificationCodeEntity.builder()
                .channel(VerificationChannel.EMAIL)
                .scene(request.getScene())
                .identifier(email)
                .code(code)
                .expireAt(now.plus(props.getExpireMinutes(), ChronoUnit.MINUTES))
                .used(false)
                .failedAttempts(0)
                .build();
        verificationCodeRepository.save(entity);
        mailClient.sendVerificationCode(email, code, request.getScene());
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void verifyAndConsume(String email, VerificationScene scene, String code) {
        String normalizedEmail = normalize(email);
        VerificationCodeEntity verification = verificationCodeRepository
                .findFirstByIdentifierAndChannelAndSceneAndUsedFalseOrderByCreatedAtDesc(
                        normalizedEmail, VerificationChannel.EMAIL, scene)
                .orElseThrow(() -> new BizException(BizErrorCode.OTP_INVALID));
        Instant now = clock.instant();

        // 检查是否已被锁定
        if (verification.getLockedAt() != null) {
            if (verification.getLockedAt().isAfter(now.minus(15, ChronoUnit.MINUTES))) {
                throw new BizException(BizErrorCode.OTP_LOCKED);
            }
            // 锁定超时，重置失败次数
            verification.setLockedAt(null);
            verification.setFailedAttempts(0);
        }

        if (verification.getExpireAt().isBefore(now)) {
            throw new BizException(BizErrorCode.OTP_EXPIRED);
        }
        if (!verification.getCode().equals(code)) {
            int failedAttempts = verification.getFailedAttempts() + 1;
            verification.setFailedAttempts(failedAttempts);
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                verification.setLockedAt(now);
            }
            verificationCodeRepository.save(verification);
            throw new BizException(BizErrorCode.OTP_INVALID);
        }
        verification.setUsed(true);
        verificationCodeRepository.save(verification);
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
