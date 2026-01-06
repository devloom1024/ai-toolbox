package com.devloom.ai.toolbox.auth.dto.request;

import com.devloom.ai.toolbox.auth.domain.enums.VerificationScene;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailCodeRequest {

    @Email
    @NotBlank
    private String email;

    @NotNull
    private VerificationScene scene;
}
