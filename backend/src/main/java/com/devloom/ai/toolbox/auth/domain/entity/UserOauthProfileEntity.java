package com.devloom.ai.toolbox.auth.domain.entity;

import com.devloom.ai.toolbox.auth.domain.enums.IdentityType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * t_user_oauth_profile 存储 LinuxDo OAuth 资料。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_user_oauth_profile")
public class UserOauthProfileEntity {

    /** 主键 ID。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 对应的用户。 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    /** OAuth 平台类型，当前固定为 LINUX_DO。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "identity_type")
    private IdentityType identityType;

    /** LinuxDo 用户 ID。 */
    @Column(name = "oauth_user_id")
    private String oauthUserId;

    /** LinuxDo 昵称。 */
    @Column(name = "oauth_username")
    private String oauthUsername;

    /** LinuxDo 邮箱。 */
    @Column(name = "oauth_email")
    private String oauthEmail;

    /** LinuxDo 头像。 */
    @Column(name = "oauth_avatar")
    private String oauthAvatar;

    /** OAuth access_token，建议加密存储。 */
    @Column(name = "access_token")
    private String accessToken;

    /** OAuth refresh_token。 */
    @Column(name = "refresh_token")
    private String refreshToken;

    /** access_token 过期时间。 */
    @Column(name = "token_expires_at")
    private Instant tokenExpiresAt;

    /** LinuxDo 返回的原始 JSON 资料。 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_profile", columnDefinition = "jsonb")
    private String rawProfile;

    /** 创建时间。 */
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    /** 更新时间。 */
    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}
