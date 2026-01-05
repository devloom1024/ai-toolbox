package com.devloom.ai.toolbox;

import org.springframework.boot.SpringApplication;

public class TestAiToolboxApplication {

    public static void main(String[] args) {
        SpringApplication.from(AiToolboxApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
