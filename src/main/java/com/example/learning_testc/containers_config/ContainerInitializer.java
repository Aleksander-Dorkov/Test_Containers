package com.example.learning_testc.containers_config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Profile("local")
public class ContainerInitializer implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        if (Arrays.asList(environment.getActiveProfiles()).contains("local")) {
            startContainers();
        }
    }

    private void startContainers() {
        ContainersConfig containersConfig = new ContainersConfig();
        containersConfig.postgresContainer().start();
        containersConfig.redisContainer().start();
        containersConfig.zookeeperContainer().start();
        containersConfig.kafkaContainer(containersConfig.zookeeperContainer()).start();
    }
}
