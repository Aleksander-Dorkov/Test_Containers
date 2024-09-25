package com.example.learning_testc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

@SpringBootApplication
public class LearningTestcApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext run = SpringApplication.run(LearningTestcApplication.class, args);
		ConfigurableEnvironment environment = run.getEnvironment();
	}
}
