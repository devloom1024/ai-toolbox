package com.devloom.ai.toolbox.investment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 自选股分页列表响应
 *
 * @author huangkl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistResponse {

    /**
     * 当前页码，从 1 开始
     */
    private Integer page;

    /**
     * 每页数据条数
     */
    private Integer pageSize;

    /**
     * 总数据条数
     */
    private Long total;

    /**
     * 当前页数据列表
     */
    private List<WatchlistItem> items;
}
