package com.devloom.ai.toolbox.auth.service;

import com.devloom.ai.toolbox.auth.domain.enums.IdentityType;
import com.devloom.ai.toolbox.auth.domain.enums.LoginStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * 登录审计上下文。
 *
 * @author devloom
 */
@Getter
@Builder
public class LoginAuditContext {

    /** 认证方式。 */
    private IdentityType type;

    /** 登录标识（邮箱/用户名等）。 */
    private String identifier;

    /** 客户端 IP 地址。 */
    private String ip;

    /** 客户端 User Agent。 */
    private String userAgent;

    /** 登录状态。 */
    private LoginStatus status;
}
