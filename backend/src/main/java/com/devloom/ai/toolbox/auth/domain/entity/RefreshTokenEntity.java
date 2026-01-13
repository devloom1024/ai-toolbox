package com.devloom.ai.toolbox.auth.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * t_refresh_token 记录 Refresh Token。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_refresh_token")
public class RefreshTokenEntity {

    /** 主键 ID。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属用户。 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    /** Refresh Token 值（用于查找，数据库中实际存储哈希值）。 */
    @Column(name = "token", length = 64)
    private String token;

    /** Refresh Token 的哈希值（用于验证，防止 token 泄露）。 */
    @Column(name = "token_hash", length = 64)
    private String tokenHash;

    /** 设备标识。 */
    private String device;

    /** 到期时间。 */
    @Column(name = "expires_at")
    private Instant expiresAt;

    /** 创建时间。 */
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
