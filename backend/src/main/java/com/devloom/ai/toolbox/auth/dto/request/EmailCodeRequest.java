package com.devloom.ai.toolbox.auth.dto.request;

import com.devloom.ai.toolbox.auth.domain.enums.VerificationScene;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 请求邮箱验证码参数。
 */
@Getter
@Setter
public class EmailCodeRequest {

    /** 接收验证码的邮箱。 */
    @Email
    @NotBlank
    private String email;

    /** 业务场景，例如注册或重置密码。 */
    @NotNull
    private VerificationScene scene;
}
