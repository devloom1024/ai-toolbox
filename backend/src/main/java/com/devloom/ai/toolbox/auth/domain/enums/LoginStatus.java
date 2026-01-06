package com.devloom.ai.toolbox.auth.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LoginStatus {
    SUCCESS(1),
    FAILURE(2);

    private final int value;
}
