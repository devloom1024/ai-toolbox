package com.devloom.ai.toolbox.auth.domain.entity;

import com.devloom.ai.toolbox.auth.domain.enums.VerificationChannel;
import com.devloom.ai.toolbox.auth.domain.enums.VerificationScene;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * t_verification_code 保存验证码记录。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_verification_code")
public class VerificationCodeEntity {

    /** 主键 ID。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 渠道，目前仅 EMAIL。 */
    @Enumerated(EnumType.STRING)
    private VerificationChannel channel;

    /** 使用场景：REGISTER / RESET_PASSWORD。 */
    @Enumerated(EnumType.STRING)
    private VerificationScene scene;

    /** 标识符（邮箱地址）。 */
    private String identifier;

    /** 验证码内容。 */
    private String code;

    /** 过期时间。 */
    @Column(name = "expire_at")
    private Instant expireAt;

    /** 是否已使用。 */
    private boolean used;

    /** 验证失败次数，超过阈值则锁定。 */
    @Column(name = "failed_attempts")
    private int failedAttempts;

    /** 锁定时间，达到最大失败次数后锁定。 */
    @Column(name = "locked_at")
    private Instant lockedAt;

    /** 创建时间。 */
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
