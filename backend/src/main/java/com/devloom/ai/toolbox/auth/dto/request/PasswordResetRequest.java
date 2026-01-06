package com.devloom.ai.toolbox.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Pattern(regexp = "\\d{6}")
    private String code;

    @NotBlank
    @Size(min = 8, max = 64)
    private String newPassword;

    @NotBlank
    @Size(min = 8, max = 64)
    private String confirmPassword;
}
