package com.devloom.ai.toolbox.investment.dto.response;

import com.devloom.ai.toolbox.investment.domain.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 投资账号响应
 *
 * @author huangkl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    /**
     * 账号 ID
     */
    private Long id;

    /**
     * 账号类型
     */
    private AccountType accountType;

    /**
     * 账号名称
     */
    private String accountName;

    /**
     * 账号图标 URL
     */
    private String accountIcon;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 是否隐藏
     */
    private Boolean isHidden;

    /**
     * 排序序号
     */
    private Short sortOrder;

    /**
     * 持仓数量
     */
    private Integer positionCount;

    /**
     * 总资产
     */
    private BigDecimal totalAssets;

    /**
     * 创建时间
     */
    private OffsetDateTime createdAt;
}
