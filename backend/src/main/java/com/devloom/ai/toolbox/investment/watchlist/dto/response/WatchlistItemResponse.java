package com.devloom.ai.toolbox.investment.watchlist.dto.response;

import com.devloom.ai.toolbox.investment.common.domain.enums.MarketType;
import com.devloom.ai.toolbox.investment.common.domain.enums.SecurityType;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

/**
 * 自选项响应。
 *
 * @author devloom
 */
@Getter
@Builder
public class WatchlistItemResponse {

    /** 自选记录 ID。 */
    private Long id;

    /** 所属分组 ID。 */
    private Long groupId;

    /** 标的代码。 */
    private String symbol;

    /** 标的名称。 */
    private String name;

    /** 市场类型。 */
    private MarketType market;

    /** 标的类型。 */
    private SecurityType type;

    /** 备注。 */
    private String note;

    /** 当前价。 */
    private BigDecimal currentPrice;

    /** 涨跌幅（%）。 */
    private BigDecimal changePercent;

    /** 添加时间。 */
    private Instant addedAt;
}
