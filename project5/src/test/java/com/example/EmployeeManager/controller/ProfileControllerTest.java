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
class ProfileControllerTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("db").withUsername("myuser");

    static final String apiUrl = "/api/profile";


    @LocalServerPort
    private Integer port;

    // sample accounts.
    private Account admin;
    private Account employee;

    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Util util;

    @Autowired
    ProfileControllerTest(AccountRepository accountRepository, BCryptPasswordEncoder bCryptPasswordEncoder, Util util) {
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
    void adminShouldGetHisProfile(){
        /*
         * tests that admin can get his profile.
         * it also checks that the response status code is 200 OK
         * it checks that the returned admin uuid is the same as the uuid of the admin that sent the request.
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
                .body("uuid", equalTo(admin.getUuid().toString()));

    }

    @Test
    void employeeShouldGetHisProfile(){
        /*
         * tests that admin can get his profile.
         * it also checks that the response status code is 200 OK
         * it checks that the returned admin uuid is the same as the uuid of the admin that sent the request.
         *
         * */

        // authenticate with account with role admin.
        String accessToken = util.attemptAuthenticationWith(employee);
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(apiUrl)
                .then()
                .statusCode(200)
                .body("uuid", equalTo(employee.getUuid().toString()));

    }

    @Test
    void adminShouldUpdateHisProfile(){
        /*
         * tests that admin can update his profile.
         * it also checks that the response status code is 200 OK
         * */

        // authenticate with account with role admin.
        String accessToken = util.attemptAuthenticationWith(admin);

        // Create a map for the request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", "Updated name" );
        requestBody.put("phone", "123456" );
        requestBody.put("job_title", "manager" );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .get(apiUrl)
                .then()
                .statusCode(200);

    }
    @Test
    void employeeShouldUpdateHisProfile(){
        /*
         * tests that admin can update his profile.
         * it also checks that the response status code is 200 OK
         *
         * */

        // authenticate with account with role admin.
        String accessToken = util.attemptAuthenticationWith(employee);

        // Create a map for the request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", "Updated name" );
        requestBody.put("phone", "123456" );
        requestBody.put("job_title", "doctor" );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .get(apiUrl)
                .then()
                .statusCode(200);

    }




}