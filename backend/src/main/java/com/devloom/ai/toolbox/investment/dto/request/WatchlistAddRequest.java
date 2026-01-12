package com.devloom.ai.toolbox.investment.dto.request;

import com.devloom.ai.toolbox.investment.domain.enums.Market;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加自选股请求
 *
 * @author huangkl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistAddRequest {

    /**
     * 股票/基金代码
     */
    @NotBlank(message = "{validation.symbol.required}")
    private String symbol;

    /**
     * 市场类型
     */
    @NotNull(message = "{validation.market.required}")
    private Market market;

    /**
     * 股票/基金名称，不传则自动查询
     */
    private String name;

    /**
     * 分组 ID，不传则添加到默认分组（未分组）
     */
    private Long groupId;
}
