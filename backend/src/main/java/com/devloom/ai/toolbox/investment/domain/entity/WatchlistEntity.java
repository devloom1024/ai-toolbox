package com.devloom.ai.toolbox.investment.domain.entity;

import com.devloom.ai.toolbox.investment.domain.enums.Market;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 自选股实体
 *
 * @author huangkl
 */
@Entity
@Table(name = "t_watchlist")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistEntity {

    /**
     * 记录主键
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
     * 关联 t_watchlist_group.id，可为空（未分组）
     */
    @Column(name = "group_id")
    private Long groupId;

    /**
     * 股票/基金代码
     */
    @Column(name = "symbol", nullable = false, length = 32)
    private String symbol;

    /**
     * 市场类型：A_SHARE=A股 HK=港股 US=美股 ETF=ETF基金 FUND=场外基金
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "market", nullable = false, length = 16)
    private Market market;

    /**
     * 股票/基金名称
     */
    @Column(name = "name", nullable = false, length = 128)
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
        if (name == null) {
            name = "";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
