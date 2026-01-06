package com.devloom.ai.toolbox.auth.dto.request;

import com.devloom.ai.toolbox.auth.domain.enums.IdentityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank
    private String identifier;

    private IdentityType type = IdentityType.EMAIL;

    @NotBlank
    @Size(min = 8, max = 64)
    private String password;

    private String device;
}
