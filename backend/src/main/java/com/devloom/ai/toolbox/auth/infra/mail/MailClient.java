package com.devloom.ai.toolbox.auth.infra.mail;

import com.devloom.ai.toolbox.auth.domain.enums.VerificationScene;

public interface MailClient {

    void sendVerificationCode(String email, String code, VerificationScene scene);
}
