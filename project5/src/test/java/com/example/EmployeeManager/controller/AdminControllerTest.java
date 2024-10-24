package com.example.EmployeeManager.controller;

import com.example.EmployeeManager.dao.AccountRepository;
import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.model.Role;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:tc:postgres:latest:///db",
                "spring.sql.init.mode=always"})
// to make sure that spring boot doesn't override the configuration and creates h2 database or any other in memory
// database.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class AdminControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("db").withUsername("myuser");


    static final String apiUrl = "/api/admin";


    @LocalServerPort
    private Integer port;

    // sample accounts.
    private Account admin;
    private Account employee;

    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Util util;

    @Autowired
    AdminControllerTest(AccountRepository accountRepository, BCryptPasswordEncoder bCryptPasswordEncoder, Util util) {
        this.accountRepository = accountRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.util = util;
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
    void adminShouldAddAdmin() {
        /*
         * tests that an account of role Admin can create admin account
         * tests that the response status code is 201 (created).
         */

        // authenticate with admin account.
        String accessToken = util.attemptAuthenticationWith(admin);

        // Create a map for the request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", "e3@email.com");
        requestBody.put("password", "123");
        requestBody.put("role", Role.ADMIN);
        requestBody.put("name", "Ahmed");


        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .post(apiUrl)
                .then()
                .statusCode(201);

    }
    @Test
    void adminShouldNotAddAdminWithoutEmail() {
        /*
         * tests that an account of role Admin can not create admin account without adding the email in the request body
         * tests that the response status code is 400 (BAD_REQUEST).
         */

        // authenticate with admin account.
        String accessToken = util.attemptAuthenticationWith(admin);

        // Create a map for the request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("password", "123");
        requestBody.put("role", Role.ADMIN);
        requestBody.put("name", "Ahmed");


        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .post(apiUrl)
                .then()
                .statusCode(400);

    }
    @Test
    void adminShouldNotAddAdminWithoutPassword() {
        /*
         * tests that an account of role Admin can not create admin account without adding the password in the request body
         * tests that the response status code is 400 (BAD_REQUEST).
         */

        // authenticate with admin account.
        String accessToken = util.attemptAuthenticationWith(admin);

        // Create a map for the request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", "e3@email.com");
        requestBody.put("role", Role.ADMIN);
        requestBody.put("name", "Ahmed");


        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .post(apiUrl)
                .then()
                .statusCode(400);

    }
    @Test
    void adminShouldNotAddAdminWithDuplicateEmail() {
        /*
         * tests that an account of role Admin can not create admin account with duplicate email in the request body
         * tests that the response status code is 409 (CONFLICT).
         */

        // authenticate with admin account.
        String accessToken = util.attemptAuthenticationWith(admin);

        // Create a map for the request body
        Map<String, Object> requestBody = new HashMap<>();

        requestBody.put("email", admin.getEmail());// admin is an admin account in the database
        requestBody.put("password", "123");
        requestBody.put("role", Role.ADMIN);
        requestBody.put("name", "Ahmed");


        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .post(apiUrl)
                .then()
                .statusCode(409);

    }


    @Test
    void employeeShouldNotAddAdmin(){
        /*
         * tests that account of role employee can't add admin account
         * test that the response code is 401 unauthorized
         * */

        // authenticate with account with role employee.
        String accessToken = util.attemptAuthenticationWith(employee);

        // Create a map for the request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", "e3@email.com");
        requestBody.put("password", "123");
        requestBody.put("role", Role.ADMIN);
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
    void adminShouldGetAllAdmins(){
        /*
         * tests that admin can get list of all admins.
         * it also checks that the response status code is 200 OK
         * it checks that the size of the returned list is 1 as we have created only one admin.
         *
         * */

        // authenticate with account with role admin.
        String accessToken = util.attemptAuthenticationWith(admin);
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(apiUrl)
                .then()
                .statusCode(200)
                .body("", hasSize(1));

    }

    @Test
    void employeeShouldNotGetAllAdmins(){
        /*
         * tests that account with role employee can't get list of all admins.
         * it also checks that the response status code is 401 Unauthorized
         * */

        // authenticate with account with role admin.
        String accessToken = util.attemptAuthenticationWith(employee);
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(apiUrl)
                .then()
                .statusCode(401);

    }
    @Test
    void adminShouldGetAdminByUUID(){
        /*
         * tests that admin can get admin by uuid.
         * checks that the response status code is 200 OK.
         * checks that the uuid of the returned admin is the same as the uuid sent in the path variable
         * */

        // authenticate with account with role admin.
        String accessToken = util.attemptAuthenticationWith(admin);
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(apiUrl + "/" + admin.getUuid())
                .then()
                .statusCode(200)
                .body("uuid", equalTo(admin.getUuid().toString()));
    }

    @Test
    void employeeShouldNotGetAdminByUuid(){
        /*
         * tests that employee can't get admin by uuid.
         * checks that the response status code is 401 unauthorized.
         * */

        // authenticate with account with role employee.
        String accessToken = util.attemptAuthenticationWith(employee);
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(apiUrl + "/" + employee.getUuid())
                .then()
                .statusCode(401);
    }

}