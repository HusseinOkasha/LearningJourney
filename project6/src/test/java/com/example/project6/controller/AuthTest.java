package com.example.project6.controller;


import com.example.project6.Enum.Role;
import com.example.project6.Service.AccountService;
import com.example.project6.Util;
import com.example.project6.entity.Account;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;
import java.util.Objects;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// to make sure that spring boot doesn't override the configuration and creates h2 database or any other in memory
// database.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class AuthTest {

    @LocalServerPort
    private Integer port;
    private final DynamoDbClient dynamoDbClient;
    private final AccountService accountService;
    private final Util util;
    private List<Account>sampleAccounts;

    static final String apiUrl = "/api/auth";
    private static LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.2"))
            .withServices(LocalStackContainer.Service.DYNAMODB);

    @Autowired
    AuthTest(DynamoDbClient dynamoDbClient, AccountService accountService, Util util) {
        this.dynamoDbClient = dynamoDbClient;
        this.accountService = accountService;
        this.util = util;
    }

    @BeforeAll
    static void setup() {
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
    void adminShouldAuthenticate() {
        sampleAccounts.forEach((account)-> System.out.println(account.getEmail() +" " + account.getPassword()));
        System.out.println(sampleAccounts);
        util.attemptAuthenticationWith(sampleAccounts.get(0));
    }

    @Test
    void employeeShouldAuthenticate() {
        util.attemptAuthenticationWith(sampleAccounts.get(2));
    }

    @Test
    void UnExistentUserShouldNotAuthenticate(){
        // attempt authentication with an account which doesn't exist on the database.
        Account nonExistentAccount = Account.builder()
                .withEmail("e5@email.com")
                .withPassword("123")
                .build();

        given()
                .contentType(ContentType.JSON)
                .auth()
                .preemptive()
                .basic(nonExistentAccount.getEmail(), nonExistentAccount.getPassword())
                .when()
                .post("/api/auth").then().statusCode(HttpStatus.UNAUTHORIZED.value());
    }

}
