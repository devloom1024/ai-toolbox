package com.devloom.ai.toolbox.investment.watchlist.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 自选分组响应。
 *
 * @author devloom
 */
@Getter
@Builder
public class WatchlistGroupResponse {

    /** 分组 ID。 */
    private Long id;

    /** 分组名称。 */
    private String name;

    /** 是否为默认分组。 */
    private Boolean isDefault;

    /** 分组内标的数量。 */
    private Integer securityCount;

    /** 排序权重（数值越小越靠前）。 */
    private Integer sortOrder;

    /** 创建时间。 */
    private java.time.Instant createdAt;

    /** 更新时间。 */
    private java.time.Instant updatedAt;
}
