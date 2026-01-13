package com.devloom.ai.toolbox.auth.domain.entity;

import com.devloom.ai.toolbox.auth.domain.enums.UserStatus;
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
 * 用户画像表 t_user。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_user")
public class UserEntity {

    /** 主键 ID。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户昵称。 */
    private String nickname;

    /** 头像 URL。 */
    private String avatar;

    /** 账号状态（ACTIVE/LOCKED/DELETED）。 */
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    /** 登录失败次数。 */
    @Column(name = "failed_login_attempts")
    private int failedLoginAttempts;

    /** 账户锁定时间。 */
    @Column(name = "locked_at")
    private Instant lockedAt;

    /** 创建时间。 */
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    /** 更新时间。 */
    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}
