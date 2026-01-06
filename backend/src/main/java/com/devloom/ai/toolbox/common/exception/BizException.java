package com.devloom.ai.toolbox.common.exception;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {

    private final BizErrorCode errorCode;
    private final transient Object[] messageArgs;

    public BizException(BizErrorCode errorCode, Object... messageArgs) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.messageArgs = messageArgs;
    }
}
