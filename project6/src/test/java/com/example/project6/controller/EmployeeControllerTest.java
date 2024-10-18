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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
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
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// to make sure that spring boot doesn't override the configuration and creates h2 database or any other in memory
// database.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class EmployeeControllerTest {
    @LocalServerPort
    private Integer port;

    private final DynamoDbClient dynamoDbClient;
    private final AccountService accountService;
    private final Util util;
    private List<Account> sampleAccounts;

    static final String API_URL = "/api/admin/employees";
    private static LocalStackContainer localStack =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.2"))
                    .withServices(LocalStackContainer.Service.DYNAMODB);

    @Autowired
    EmployeeControllerTest(DynamoDbClient dynamoDbClient, AccountService accountService, Util util) {
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
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        dynamoDbClient.createTable(util.buildCreateTableRequest());

        // returns a list of 4 account first 2 accounts are admins, last 2 account are employees
        sampleAccounts = Util.buildAccounts();

        // save sample accounts to the database.
        sampleAccounts.forEach(accountService::save);
    }

    @AfterEach
    void tearDown() {
        dynamoDbClient.deleteTable(DeleteTableRequest.builder().tableName("app").build());
    }

    @Test
    void adminShouldAddEmployee() {
        /*
         * tests that an account of role Admin can create accounts
         * tests that the response status code is 201 (created).
         */
        Account admin = sampleAccounts.get(0);
        // authenticate with admin account.
        String accessToken = util.attemptAuthenticationWith(admin);

        // Create a map for the request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", "e3@email.com");
        requestBody.put("password", "123");
        requestBody.put("role", Role.EMPLOYEE);
        requestBody.put("name", "Ahmed");


        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .post(API_URL)
                .then()
                .statusCode(HttpStatus.CREATED.value());
    }

    @ParameterizedTest
    @MethodSource("provideAccountsForAddEmployeeWithInValidDataTest")
    void shouldNotAddEmployeeWithInValidData(Account account, int expectedStatusCode,
                                          Map<String, Object> requestBody){
        /*
         * account: is the account which will attempt to create new admin account.
         * expectedStatusCode: is the status code that should be returned with the response.
         * requestBody: contains the attributes of the new account.
         * */


        // attempt authentication and get an access token.
        String accessToken = util.attemptAuthenticationWith(account);

        // send post request to endpoint "/api/admin" with a request body containing the new admin account
        // attributes, along with a bearer token.
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken).body(requestBody)
                .when()
                .request(HttpMethod.POST.toString(), API_URL)
                .then()
                .statusCode(expectedStatusCode);

    }
    static Stream<Arguments> provideAccountsForAddEmployeeWithInValidDataTest(){

        Map.Entry<String, Object> email = Map.entry("email", "newEmployee@email.com");
        Map.Entry<String, Object> password = Map.entry("password", "password123");
        Map.Entry<String, Object> role = Map.entry("role", Role.EMPLOYEE);
        Map.Entry<String, Object> name = Map.entry("name", "New employee");

        // returns a list of 4 accounts first 2 are admins last 2 are employees
        List<Account> sampleAccounts = Util.buildAccounts();
        Account admin = sampleAccounts.get(0);
        return Stream.of(
                Arguments.of(admin, HttpStatus.BAD_REQUEST.value(), Map.ofEntries(password, role, name)), // create employee without email
                Arguments.of(admin, HttpStatus.BAD_REQUEST.value(),  Map.ofEntries(email,role, name)) , // create employee without password
                Arguments.of(admin, HttpStatus.BAD_REQUEST.value(),  Map.ofEntries(email, password, name)) // create employee without role.
        );
    }



}