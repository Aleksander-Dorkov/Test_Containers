package com.example.learning_testc.containers_config;

import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

@Configuration
@Profile("local")
public class ContainersConfig {

    private static final Network KAFKA_NETWORK = Network.newNetwork();

    @Bean
    public static BeanFactoryPostProcessor dependsOnPostProcessor() {
        return bf -> {
            String[] postgres = {"entityManagerFactory", "dataSource", "sqlSessionFactory"};
            for (String bean : postgres) {
                if (bf.containsBean(bean)) {
                    bf.getBeanDefinition(bean).setDependsOn("postgresContainer");
                }
            }
        };
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public GenericContainer<?> postgresContainer() {
        return new GenericContainer<>(DockerImageName.parse("postgres:14.13-alpine"))
                .withEnv("POSTGRES_USER", "sasho")
                .withEnv("POSTGRES_PASSWORD", "1234")
                .withEnv("POSTGRES_DB", "my_db_name")
                .withExposedPorts(5432)
                .withCreateContainerCmdModifier(cmd -> cmd
                        .withName("my-postgres")
                        .withHostConfig(
                                HostConfig.newHostConfig().withPortBindings(PortBinding.parse("5432:5432"))
                        ));
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis:7.2.1"))
                .withExposedPorts(6379)
                .withCreateContainerCmdModifier(cmd -> cmd
                        .withName("my-redis")
                        .withHostConfig(
                                HostConfig.newHostConfig().withPortBindings(PortBinding.parse("6379:6379"))
                        ));
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public GenericContainer<?> zookeeperContainer() {
        return new GenericContainer<>(DockerImageName.parse("confluentinc/cp-zookeeper:7.3.0"))
                .withNetwork(KAFKA_NETWORK)
                .withNetworkAliases("zookeeper")
                .withEnv("ZOOKEEPER_CLIENT_PORT", "2181")
                .withEnv("ZOOKEEPER_TICK_TIME", "2000")
                .withCreateContainerCmdModifier(cmd -> {
                    cmd.withName("my-zookeeper");
                });
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public GenericContainer<?> kafkaContainer(GenericContainer<?> zookeeperContainer) {
        return new GenericContainer<>(DockerImageName.parse("confluentinc/cp-kafka:7.3.0"))
                .withNetwork(KAFKA_NETWORK)
                .withNetworkAliases("kafka")
                .withExposedPorts(9092)
                .withEnv("KAFKA_BROKER_ID", "1")
                .withEnv("KAFKA_ZOOKEEPER_CONNECT", "zookeeper:2181")
                .withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "PLAINTEXT:PLAINTEXT,PLAINTEXT_INTERNAL:PLAINTEXT")
                .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://localhost:9092,PLAINTEXT_INTERNAL://broker:29092")
                .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
                .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
                .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
                .withCreateContainerCmdModifier(cmd -> cmd
                        .withName("my-kafka")
                        .withHostConfig(
                                HostConfig.newHostConfig().withPortBindings(PortBinding.parse("9092:9092"))
                        ))
                .dependsOn(zookeeperContainer);
    }
}
