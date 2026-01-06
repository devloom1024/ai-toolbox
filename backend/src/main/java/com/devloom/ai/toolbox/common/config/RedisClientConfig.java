package com.devloom.ai.toolbox.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.data.redis.autoconfigure.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.protocol.ProtocolVersion;

/**
 * Force Lettuce to use RESP2 handshake so AUTH happens before HELLO.
 */
@Configuration
public class RedisClientConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisClientConfig.class);

    @Bean
    public LettuceClientConfigurationBuilderCustomizer redisProtocolCustomizer() {
        return builder -> {
            log.info("Configuring Lettuce client to force RESP2 protocol (AUTH before HELLO).");
            builder.clientOptions(
                ClientOptions.builder()
                    .protocolVersion(ProtocolVersion.RESP2)
                    .build()
            );
        };
    }
}
