package com.devloom.ai.toolbox.auth.domain.converter;

import com.devloom.ai.toolbox.auth.domain.enums.LoginStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LoginStatusConverter implements AttributeConverter<LoginStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(LoginStatus status) {
        return status == null ? null : status.getValue();
    }

    @Override
    public LoginStatus convertToEntityAttribute(Integer value) {
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
