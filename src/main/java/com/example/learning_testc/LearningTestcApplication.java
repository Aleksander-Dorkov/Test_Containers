package com.example.learning_testc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LearningTestcApplication {

    public static void main(String[] args) {
        var context = SpringApplication.run(LearningTestcApplication.class, args);
        var env = context.getEnvironment();
        var profiles = env.getActiveProfiles();
    }
}
