package com.devloom.ai.toolbox;

import com.devloom.ai.toolbox.auth.service.support.AuthProperties;
import com.devloom.ai.toolbox.common.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AuthProperties.class, JwtProperties.class})
public class AiToolboxApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiToolboxApplication.class, args);
    }

}
