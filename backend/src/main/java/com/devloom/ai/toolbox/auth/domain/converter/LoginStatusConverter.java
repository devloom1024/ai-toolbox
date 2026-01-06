package com.devloom.ai.toolbox.auth.domain.converter;

import com.devloom.ai.toolbox.auth.domain.enums.LoginStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LoginStatusConverter implements AttributeConverter<LoginStatus, Short> {

    @Override
    public Short convertToDatabaseColumn(LoginStatus status) {
        return status == null ? null : (short) status.getValue();
    }

    @Override
    public LoginStatus convertToEntityAttribute(Short value) {
        if (value == null) {
            return null;
        }
        for (LoginStatus status : LoginStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        return LoginStatus.FAILURE;
    }
}
