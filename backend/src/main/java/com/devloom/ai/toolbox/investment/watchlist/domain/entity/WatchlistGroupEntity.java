package com.devloom.ai.toolbox.investment.watchlist.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * 自选分组表 t_watchlist_group。
 *
 * @author devloom
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_watchlist_group")
public class WatchlistGroupEntity {

    /** 主键 ID。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户 ID。 */
    @Column(name = "user_id")
    private Long userId;

    /** 分组名称。 */
    @Column(name = "name", length = 50)
    private String name;

    /** 是否默认分组。 */
    @Column(name = "is_default")
    private Boolean isDefault;

    /** 排序权重（数值越小越靠前）。 */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /** 创建时间。 */
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    /** 更新时间。 */
    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}
