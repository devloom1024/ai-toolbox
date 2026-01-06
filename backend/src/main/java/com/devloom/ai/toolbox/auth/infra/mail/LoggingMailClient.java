package com.devloom.ai.toolbox.auth.infra.mail;

import com.devloom.ai.toolbox.auth.domain.enums.VerificationScene;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingMailClient implements MailClient {

    @Override
    public void sendVerificationCode(String email, String code, VerificationScene scene) {
        log.info("[MAIL] send verification code scene={} email={} code={}", scene, email, code);
    }
}
