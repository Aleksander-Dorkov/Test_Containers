package com.example.learning_testc;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CustomerControllerTest2 {

    @LocalServerPort
    private Integer port;

    public static final GenericContainer<?> PG_14 = new GenericContainer<>(DockerImageName.parse("postgres:14.13-alpine"))
            .withEnv("POSTGRES_USER", "sasho")
            .withEnv("POSTGRES_PASSWORD", "1234")
            .withEnv("POSTGRES_DB", "forum")
            .withExposedPorts(5432);

    @BeforeAll
    public static void beforeAll() {
        PG_14.start(); // creates the container
    }

    @AfterAll
    public static void afterAll() {
        PG_14.stop(); // Deletes the container completely, Does not have persistent volumes
    }

    @DynamicPropertySource
    public static void configureProperties(DynamicPropertyRegistry registry) {
        String postgresDb = PG_14.getEnvMap().get("POSTGRES_DB");
//        int port = PG_14.getExposedPorts().getFirst();
        int mappedPort = PG_14.getMappedPort(5432); // Use this instead of PG_14.getExposedPorts().getFirst(); because this will fetch the mapped port on the machine
        System.out.println(postgresDb + ":" + mappedPort);
        registry.add("spring.datasource.url", () ->
                "jdbc:postgresql://localhost:%s/%s".formatted(mappedPort, postgresDb)
        );
        registry.add("spring.datasource.username", () -> PG_14.getEnvMap().get("POSTGRES_USER"));
        registry.add("spring.datasource.password", () -> PG_14.getEnvMap().get("POSTGRES_PASSWORD"));
    }

    @Autowired
    CustomerRepository customerRepository;

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
