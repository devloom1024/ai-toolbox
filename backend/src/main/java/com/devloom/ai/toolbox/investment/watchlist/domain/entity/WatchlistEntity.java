package com.devloom.ai.toolbox.investment.watchlist.domain.entity;

import com.devloom.ai.toolbox.investment.watchlist.domain.enums.MarketType;
import com.devloom.ai.toolbox.investment.watchlist.domain.enums.SecurityType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 自选记录表 t_watchlist。
 *
 * @author claude
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_watchlist")
public class WatchlistEntity {

    /** 主键 ID。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户 ID。 */
    @Column(name = "user_id")
    private Long userId;

    /** 分组 ID。 */
    @Column(name = "group_id")
    private Long groupId;

    /** 标的代码（纯数字或字母）。 */
    @Column(name = "symbol", length = 32)
    private String symbol;

    /** 标的名称。 */
    @Column(name = "name", length = 128)
    private String name;

    /** 市场类型。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "market", length = 16)
    private MarketType market;

    /** 标的类型。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 16)
    private SecurityType type;

    /** 备注。 */
    @Column(name = "note", length = 200)
    private String note;

    /** 当前价。 */
    @Column(name = "current_price", precision = 20, scale = 4)
    private BigDecimal currentPrice;

    /** 涨跌幅（%）。 */
    @Column(name = "change_percent", precision = 10, scale = 4)
    private BigDecimal changePercent;

    /** 添加时间。 */
    @Column(name = "added_at")
    private Instant addedAt;
}
