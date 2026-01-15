package com.devloom.ai.toolbox.investment.watchlist.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * 更新分组请求参数，对应 /api/v1/investment/watchlist/groups/{id} (PUT)。
 *
 * @author devloom
 */
@Getter
@Setter
public class UpdateGroupRequest {

    /** 分组新名称。 */
    private String name;
}
