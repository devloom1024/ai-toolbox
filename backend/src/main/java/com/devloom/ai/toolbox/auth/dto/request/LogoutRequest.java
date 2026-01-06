package com.devloom.ai.toolbox.auth.dto.request;

import com.devloom.ai.toolbox.auth.domain.enums.LogoutScope;
import lombok.Getter;
import lombok.Setter;

/**
 * 登出请求参数。
 */
@Getter
@Setter
public class LogoutRequest {

    /** 登出范围，默认仅当前设备。 */
    private LogoutScope scope = LogoutScope.CURRENT;
}
