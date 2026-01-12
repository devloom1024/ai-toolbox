package com.devloom.ai.toolbox.investment.domain.entity;

import com.devloom.ai.toolbox.investment.domain.enums.AccountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 投资账号实体
 *
 * @author huangkl
 */
@Entity
@Table(name = "t_account")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntity {

    /**
     * 账号主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联 t_user.id
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 账号类型：BROKER=券商 FUND_PLATFORM=基金平台 BANK=银行 ALIPAY=支付宝 OTHER=其他
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 32)
    private AccountType accountType;

    /**
     * 账号名称（用户自定义，如"东方财富A股"）
     */
    @Column(name = "account_name", nullable = false, length = 64)
    private String accountName;

    /**
     * 账号图标 URL
     */
    @Column(name = "account_icon", nullable = false, length = 512)
    private String accountIcon;

    /**
     * 是否启用
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /**
     * 是否隐藏（隐藏后不在列表显示）
     */
    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden;

    /**
     * 排序序号
     */
    @Column(name = "sort_order", nullable = false)
    private Short sortOrder;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (accountIcon == null) {
            accountIcon = "";
        }
        if (isActive == null) {
            isActive = true;
        }
        if (isHidden == null) {
            isHidden = false;
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
