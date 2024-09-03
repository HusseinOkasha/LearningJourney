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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:tc:postgres:latest:///db",
                "spring.sql.init.mode=always"})
// to make sure that spring boot doesn't override the configuration and creates h2 database or any other in memory
// database.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmployeeControllerTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("db").withUsername("myuser");

    @LocalServerPort
    private Integer port;

    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private Account admin;
    private Account user;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    EmployeeControllerTest(AccountRepository accountRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.accountRepository = accountRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @BeforeEach
    void setUp() {

        RestAssured.baseURI = "http://localhost:" + port;

        // create sample accounts.
        admin = Account
                .builder()
                .withEmail("e1@email.com")
                .withName("hussein")
                .withRole(Role.ADMIN)
                .withPassword(bCryptPasswordEncoder.encode("123"))
                .build();

        user = Account.builder()
                .withEmail("e2@email.com")
                .withName("ahmed")
                .withRole(Role.USER)
                .withPassword(bCryptPasswordEncoder.encode("123"))
                .build();

        // save sample accounts to the database.
        accountRepository.save(admin);
        accountRepository.save(user);

    }

    @AfterEach
    void tearDown() {
        accountRepository.deleteAll();

    }

    @Test
    void adminShouldAddAccount() {
        /*
         * tests that an account of role Admin can create accounts
         * tests that the response status code is 201 (created).
         */

        // authenticate with admin account.
        String accessToken = attemptAuthenticationWith(admin);

        // Create a map for the request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", "e3@email.com");
        requestBody.put("password", "123");
        requestBody.put("role", "USER");
        requestBody.put("name", "Ahmed");


        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .post("/api/employee")
                .then()
                .statusCode(201);

    }

    @Test
    void userShouldNotAddAccount(){
        /*
        * tests that account of type user can't create accounts
        * test that the response code is 401 unauthorized
        * */

        // authenticate with account with role user.
        String accessToken = attemptAuthenticationWith(user);

        // Create a map for the request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", "e3@email.com");
        requestBody.put("password", "123");
        requestBody.put("role", "USER");
        requestBody.put("name", "Ahmed");


        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .post("/api/employee")
                .then()
                .statusCode(401);


    }

    @Test
    void adminShouldGetAllEmployees(){
        /*
        * tests that admin can get list of all employees.
        * it also checks that the response status code is 200 OK
        * it checks that the size of the returned list is 1 as we have created only one employee.
        *
        * */

        // authenticate with account with role admin.
        String accessToken = attemptAuthenticationWith(admin);
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/employee")
                .then()
                .statusCode(200)
                .body("", hasSize(1));

    }

    @Test
    void userShouldNotGetAllEmployees(){
        /*
         * tests that account with role user can't get list of all employees.
         * it also checks that the response status code is 401 Unauthorized
         * */

        // authenticate with account with role admin.
        String accessToken = attemptAuthenticationWith(user);
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/employee")
                .then()
                .statusCode(401);

    }

    static String attemptAuthenticationWith(Account account) {
        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .auth()
                        .preemptive()
                        .basic(account.getEmail(), "123")
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

        return accessToken;
    }


}