package com.devloom.ai.toolbox.investment.watchlist.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * 创建分组请求参数，对应 /api/v1/investment/watchlist/groups (POST)。
 *
 * @author devloom
 */
@Getter
@Setter
public class CreateGroupRequest {

    /** 分组名称。 */
    private String name;

    /** 排序权重（可选）。 */
    private Integer sortOrder = 0;
}
