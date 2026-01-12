package com.devloom.ai.toolbox.investment.dto.request;

import com.devloom.ai.toolbox.investment.domain.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新投资账号请求
 *
 * @author huangkl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdateRequest {

    /**
     * 账号名称
     */
    private String accountName;

    /**
     * 账号类型
     */
    private AccountType accountType;

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
}
