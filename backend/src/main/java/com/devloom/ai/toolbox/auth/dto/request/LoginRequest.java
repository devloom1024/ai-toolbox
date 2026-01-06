package com.devloom.ai.toolbox.auth.dto.request;

import com.devloom.ai.toolbox.auth.domain.enums.IdentityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 登录请求参数，对应 /api/v1/auth/login。
 */
@Getter
@Setter
public class LoginRequest {

    /** 登录标识，目前仅支持邮箱地址。 */
    @NotBlank(message = "{validation.identifier.required}")
    private String identifier;

    /** 登录类型，默认为 EMAIL，预留扩展。 */
    private IdentityType type = IdentityType.EMAIL;

    /** 登录密码，需满足最小长度。 */
    @NotBlank(message = "{validation.password.required}")
    @Size(min = 8, max = 64, message = "{validation.password.length}")
    private String password;
}
