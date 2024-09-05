package com.example.EmployeeManager.controller;

import com.example.EmployeeManager.dao.AccountRepository;
import com.example.EmployeeManager.dto.TaskDto;
import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.model.Role;
import com.example.EmployeeManager.model.TaskStatus;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
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
import static org.hamcrest.Matchers.equalTo;
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
class TaskControllerTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("db").withUsername("myuser");

    static final String authenticateUrl = "/api/auth/authenticate";
    static final String apiUrl = "/api/task";


    @LocalServerPort
    private Integer port;

    // sample accounts.
    private Account admin;
    private Account employee;

    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    TaskControllerTest(AccountRepository accountRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
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

        // create sample accounts.
        admin = Account
                .builder()
                .withEmail("e1@email.com")
                .withName("hussein")
                .withRole(Role.ADMIN)
                .withPassword(bCryptPasswordEncoder.encode("123"))
                .build();

        employee = Account.builder()
                .withEmail("e2@email.com")
                .withName("ahmed")
                .withRole(Role.EMPLOYEE)
                .withPassword(bCryptPasswordEncoder.encode("123"))
                .build();

        // save sample accounts to the database.
        accountRepository.save(admin);
        accountRepository.save(employee);

    }

    @AfterEach
    void tearDown() {
        accountRepository.deleteAll();

    }

    @Test
    public void adminShouldAddTask(){
        /*
        * tests that an account of role admin can create tasks for himself.
        * tests that the response status code is 201 (CREATED)
        *
        * */

        // attempt authentication with account of role ADMIN
        String accessToken = attemptAuthenticationWith(admin);

        // taskDto for the task to be created.
        TaskDto taskDto = new TaskDto("this is an important task","task 1", TaskStatus.TODO );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(taskDto)
                .when()
                .post(apiUrl)
                .then()
                .statusCode(HttpStatus.CREATED.value());
    }
    @Test
    public void employeeShouldAddTask(){
        /*
         * tests that an account of role employee can create tasks for himself.
         * tests that the response status code is 201 (CREATED)
         *
         * */

        // attempt authentication with account of role EMPLOYEE
        String accessToken = attemptAuthenticationWith(employee);

        // taskDto for the task to be created.
        TaskDto taskDto = new TaskDto("this is an important task","task 1", TaskStatus.TODO );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(taskDto)
                .when()
                .post(apiUrl)
                .then()
                .statusCode(HttpStatus.CREATED.value());
    }

    static String attemptAuthenticationWith(Account account) {
        /*
         * helper method attempt authentication with the passed account.
         * it checks that the authentication request was successful
         * by checking that the response status code is 200 OK
         * returns jwt token resulted from the authentication process.
         * */
        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .auth()
                        .preemptive()
                        .basic(account.getEmail(), "123")
                        .when()
                        .post(authenticateUrl)
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