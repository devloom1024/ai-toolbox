package com.devloom.ai.toolbox.investment.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 自选股分组实体
 *
 * @author huangkl
 */
@Entity
@Table(name = "t_watchlist_group")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistGroupEntity {

    /**
     * 分组主键
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
     * 分组名称
     */
    @Column(name = "name", nullable = false, length = 64)
    private String name;

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
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
