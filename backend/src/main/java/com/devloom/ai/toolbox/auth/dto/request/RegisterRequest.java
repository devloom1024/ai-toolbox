package com.devloom.ai.toolbox.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 请求邮箱注册参数，对应 /api/v1/auth/register。
 */
@Getter
@Setter
public class RegisterRequest {

    /** 用户邮箱，必须合法且唯一。 */
    @Email(message = "{validation.email.invalid}")
    @NotBlank(message = "{validation.email.required}")
    private String email;

    /** 登录密码，8-64 位。 */
    @NotBlank(message = "{validation.password.required}")
    @Size(min = 8, max = 64, message = "{validation.password.length}")
    private String password;

    /** 邮箱验证码，来自 /api/v1/auth/code/email。 */
    @NotBlank(message = "{validation.code.required}")
    private String code;

    /** 用户昵称。 */
    @NotBlank(message = "{validation.nickname.required}")
    private String nickname;
}
