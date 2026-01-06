package com.devloom.ai.toolbox.auth.dto.request;

import com.devloom.ai.toolbox.auth.domain.enums.LogoutScope;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutRequest {

    private LogoutScope scope = LogoutScope.CURRENT;
}
