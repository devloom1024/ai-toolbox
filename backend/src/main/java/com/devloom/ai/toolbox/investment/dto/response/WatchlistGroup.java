package com.devloom.ai.toolbox.investment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 自选股分组
 *
 * @author huangkl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistGroup {

    /**
     * 分组 ID，关联 t_watchlist_group.id
     */
    private Long id;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 排序序号
     */
    private Short sortOrder;

    /**
     * 该分组下的自选股数量
     */
    private Integer itemCount;

    /**
     * 创建时间
     */
    private OffsetDateTime createdAt;
}
