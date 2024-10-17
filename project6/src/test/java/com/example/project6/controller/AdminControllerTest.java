package com.example.project6.controller;

import com.example.project6.Enum.Role;
import com.example.project6.Service.AccountService;
import com.example.project6.Util;
import com.example.project6.entity.Account;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// to make sure that spring boot doesn't override the configuration and creates h2 database or any other in memory
// database.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class AdminControllerTest {
    @LocalServerPort
    private Integer port;

    private final DynamoDbClient dynamoDbClient;
    private final AccountService accountService;
    private final Util util;
    private List<Account> sampleAccounts;

    static final String apiUrl = "/api/admin";
    private static LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.2"))
            .withServices(LocalStackContainer.Service.DYNAMODB);

    @Autowired
    AdminControllerTest(DynamoDbClient dynamoDbClient, AccountService accountService, Util util) {
        this.dynamoDbClient = dynamoDbClient;
        this.accountService = accountService;
        this.util = util;
    }

    @BeforeAll
    static void beforeAll(){
        localStack.start();
        System.setProperty("amazon.dynamodb.endpoint",
                localStack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString());
    }

    @BeforeEach
    void beforeEach() {
        RestAssured.baseURI = "http://localhost:" + port;
        dynamoDbClient.createTable(util.buildCreateTableRequest());

        // returns a list of 4 account first 2 accounts are admins, last 2 account are employees
        sampleAccounts = util.buildAccounts();

        // save sample accounts to the database.
        sampleAccounts.forEach(accountService::save);

    }
    @AfterEach
    void afterEach(){
        dynamoDbClient.deleteTable(DeleteTableRequest.builder().tableName("app").build());
    }

    @Test
    void adminShouldCreateAdminAccount(){
        /*
         * tests that an account of role Admin can create admin account
         * tests that the response status code is 201 (created).
         */
        String adminToken = util.attemptAuthenticationWith(sampleAccounts.get(0));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", "e5@email.com");
        requestBody.put("password", "123");
        requestBody.put("role", Role.ADMIN);
        requestBody.put("name", "Ahmed");

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post(apiUrl).then().statusCode(HttpStatus.CREATED.value());
    }
    @Test
    void adminShouldNotAddAdminWithoutEmail(){
        /*
         * tests that an account of role Admin can not create admin account without adding the email in the request body
         * tests that the response status code is 400 (BAD_REQUEST).
         */
        String adminToken = util.attemptAuthenticationWith(sampleAccounts.get(0));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("password", "123");
        requestBody.put("role", Role.ADMIN);
        requestBody.put("name", "Ahmed");

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post(apiUrl).then().statusCode(HttpStatus.BAD_REQUEST.value());
    }
    @Test
    void adminShouldNotAddAdminWithoutPassword() {
        /*
         * tests that an account of role Admin can not create admin account without adding the password in the request body
         * tests that the response status code is 400 (BAD_REQUEST).
         */

        // authenticate with admin account.
        String accessToken = util.attemptAuthenticationWith(sampleAccounts.get(0));

        // Create a map for the request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", "e5@email.com");
        requestBody.put("role", Role.ADMIN);
        requestBody.put("name", "ziad");


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
    void employeeShouldNotAddAdmin(){
        /*
         * tests that account of role employee can't add admin account
         * test that the response code is 401 unauthorized
         * */
        Account employee = sampleAccounts.get(2);
        // authenticate with account with role employee.
        String accessToken = util.attemptAuthenticationWith(employee);

        // Create a map for the request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", "e5@email.com");
        requestBody.put("password", "123");
        requestBody.put("role", Role.ADMIN);
        requestBody.put("name", "ziad");


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
         * it checks that the size of the returned list is 2 as we have created only one admin.
         *
         * */

        // sampleAccounts: list of 4 account first 2 accounts are admins, last 2 account are employees
        Account admin = sampleAccounts.get(0);

        // authenticate with account with role admin.
        String accessToken = util.attemptAuthenticationWith(admin);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(apiUrl+"/all")
                .then()
                .statusCode(200)
                .body("", hasSize(2));

    }
    @Test
    void employeeShouldNotGetAllAdmins(){
        /*
         * tests that account with role employee can't get list of all admins.
         * it also checks that the response status code is 401 Unauthorized
         * */

        // sampleAccounts: list of 4 account first 2 accounts are admins, last 2 account are employees
        Account employee = sampleAccounts.get(2);

        // authenticate with account with role admin.
        String accessToken = util.attemptAuthenticationWith(employee);
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(apiUrl+"/all")
                .then()
                .statusCode(401);
    }
    @Test
    void employeeShouldNotGetAdminByUuid(){
        /*
         * tests that employee can't get admin by uuid.
         * checks that the response status code is 401 unauthorized.
         * */

        // sampleAccounts: list of 4 account first 2 accounts are admins, last 2 account are employees
        Account employee = sampleAccounts.get(2);

        // authenticate with account with role employee.
        String accessToken = util.attemptAuthenticationWith(employee);
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(apiUrl + "/" + employee.getAccountUuid())
                .then()
                .statusCode(401);
    }

}