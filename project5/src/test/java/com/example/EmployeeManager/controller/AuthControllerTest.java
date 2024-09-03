package com.example.EmployeeManager.controller;

import com.example.EmployeeManager.dao.AccountRepository;
import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.model.Role;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
        "spring.datasource.url=jdbc:tc:postgres:latest:///db",
                "spring.sql.init.mode=always"})

// to make sure that spring boot doesn't override the configuration and creates h2 database or any other in memory database.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("db").withUsername("myuser");

    @LocalServerPort
    private Integer port;

    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    AuthControllerTest(AccountRepository accountRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.accountRepository = accountRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    @BeforeAll
    static void beforeAll(){
        postgres.start();
    }

    @AfterAll
    static void afterAll(){
        postgres.stop();
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;

        Account account = Account
                .builder()
                .withEmail("e1@email.com")
                .withName("hussein")
                .withRole(Role.ADMIN)
                .withPassword(bCryptPasswordEncoder.encode("123"))
                .build();

        accountRepository.save(account);
    }

    @AfterEach
    void tearDown() {
        accountRepository.deleteAll();

    }

    @Test
    void shouldRegisterAccount(){

        // Create a map for the request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", "e2@email.com");
        requestBody.put("password", "123");
        requestBody.put("role", "ADMIN");
        requestBody.put("name", "Ahmed");

        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .body(requestBody)
                        .when()
                        .post("/api/auth/register")
                        .then()
                        .statusCode(200)
                        .body("accessToken", notNullValue())
                        .extract()
                        .response();

        String accessToken = response
                .jsonPath()
                .getString("accessToken");

        assertThat(accessToken).isNotEmpty();
    }
    @Test
    public void shouldAuthenticate(){
        /*
            * tests that the api/auth/authenticate endpoint
            * make sure that:
                * the status code is 200 (OK).
                * the response body contains jwt token.
        */

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", "e1@email.com");
        requestBody.put("password", "123");

        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .auth()
                        .preemptive()
                        .basic("e1@email.com", "123")
                        .when()
                        .post("/api/auth/authenticate")
                        .then()
                        .statusCode(200)
                        .body("accessToken", notNullValue())
                        .extract()
                        .response();

        String accessToken = response
                .jsonPath()
                .getString("accessToken");

        assertThat(accessToken).isNotEmpty();
    }
}