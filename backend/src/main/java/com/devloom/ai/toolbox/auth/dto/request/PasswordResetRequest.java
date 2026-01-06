package com.devloom.ai.toolbox.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 重置密码请求参数。
 */
@Getter
@Setter
public class PasswordResetRequest {

    /** 绑定账号的邮箱。 */
    @Email(message = "{validation.email.invalid}")
    @NotBlank(message = "{validation.email.required}")
    private String email;

    /** 邮箱验证码，6 位数字。 */
    @NotBlank(message = "{validation.code.required}")
    @Pattern(regexp = "\\d{6}", message = "{validation.code.pattern}")
    private String code;

    /** 新密码。 */
    @NotBlank(message = "{validation.new-password.required}")
    @Size(min = 8, max = 64, message = "{validation.password.length}")
    private String newPassword;

    /** 新密码确认。 */
    @NotBlank(message = "{validation.confirm-password.required}")
    @Size(min = 8, max = 64, message = "{validation.password.length}")
    private String confirmPassword;
}
