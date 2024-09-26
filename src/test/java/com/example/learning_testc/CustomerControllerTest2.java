package com.example.learning_testc;

import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;

public class CustomerControllerTest2 extends BaseClassForIntegrationTests {

    @LocalServerPort
    private Integer port;
    @Autowired
    private Environment environment;

    @Autowired
    CustomerRepository customerRepository;

    public static final GenericContainer<?> PG_14 = new GenericContainer<>(DockerImageName.parse("postgres:14.13-alpine"))
            .withEnv("POSTGRES_USER", "sasho")
            .withEnv("POSTGRES_PASSWORD", "1234")
            .withEnv("POSTGRES_DB", "forum")
            .withExposedPorts(5432)
            .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                    HostConfig.newHostConfig().withPortBindings(PortBinding.parse("5432:5432"))
            ));

    @BeforeAll
    public void beforeAll() {
        for (String activeProfile : environment.getActiveProfiles()) {
            System.out.println("active profile: " + activeProfile);
        }
        PG_14.start(); // creates the container
    }

    @AfterAll
    public void afterAll() {
        PG_14.stop(); // Deletes the container completely, Does not have persistent volumes
    }

    @DynamicPropertySource
    public static void configureProperties(DynamicPropertyRegistry registry) {

        PG_14.start();
        String postgresDb = PG_14.getEnvMap().get("POSTGRES_DB");
        int mappedPort = PG_14.getMappedPort(5432); // Use this instead of PG_14.getExposedPorts().getFirst(); because this will fetch the mapped port on the machine
        System.out.println(postgresDb + ":" + mappedPort);
        registry.add("spring.datasource.url", () ->
                "jdbc:postgresql://localhost:%s/%s".formatted(mappedPort, postgresDb)
        );
        registry.add("spring.datasource.username", () -> PG_14.getEnvMap().get("POSTGRES_USER"));
        registry.add("spring.datasource.password", () -> PG_14.getEnvMap().get("POSTGRES_PASSWORD"));
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        customerRepository.deleteAll();
    }

    @Test
    void shouldGetAllCustomers() {
        List<Customer> customers = List.of(
                new Customer(null, "John", "john@mail.com"),
                new Customer(null, "Dennis", "dennis@mail.com")
        );
        customerRepository.saveAll(customers);

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/customers")
                .then()
                .statusCode(200)
                .body(".", hasSize(2));
    }
}
