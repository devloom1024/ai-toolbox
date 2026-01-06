package com.devloom.ai.toolbox.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BindLinuxDoRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String state;
}
