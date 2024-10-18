package com.example.project6.controller;

import com.example.project6.Enum.Role;
import com.example.project6.Service.AccountService;
import com.example.project6.Util;
import com.example.project6.dto.ProfileDto;
import com.example.project6.entity.Account;
import com.example.project6.util.entityAndDtoMappers.AccountMapper;
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
import static org.hamcrest.Matchers.*;


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
        sampleAccounts = Util.buildAccounts();

        // save sample accounts to the database.
        sampleAccounts.forEach(accountService::save);

    }
    @AfterEach
    void afterEach(){
        dynamoDbClient.deleteTable(DeleteTableRequest.builder().tableName("app").build());
    }

    @ParameterizedTest
    @MethodSource("provideAccountsForCreatingNewAdminAuthorizationTest")
    void testAuthorizationForCreatingNewAdmin(Account account, int expectedStatusCode,
                                                    Map<String, Object> requestBody) {
        /*
        * account: is the account which will attempt to create new admin account.
        * expectedStatusCode: is the status code that should be returned with the response.
        * requestBody: contains the attributes of the new account.
        * */

        // endpoint to which the new account creation request will be sent to.
        final String endpoint = "/api/admin";

        // attempt authentication and get an access token.
        String accessToken = util.attemptAuthenticationWith(account);

        // send post request to endpoint "/api/admin" with a request body containing the new admin account
        // attributes, along with a bearer token.
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken).body(requestBody)
                .when()
                .request(HttpMethod.POST.toString(), endpoint)
                .then()
                .statusCode(expectedStatusCode);
    }
    private static Stream<Arguments> provideAccountsForCreatingNewAdminAuthorizationTest() {

        // request body contains the account to be created,
        // in case you send a request to create a new admin account.
        Map<String, Object> postRequestBody = new HashMap<>();
        postRequestBody.put("email", "newadmin@email.com");
        postRequestBody.put("password", "password123");
        postRequestBody.put("role", Role.ADMIN);
        postRequestBody.put("name", "New Admin");

        // returns a list of 4 accounts first 2 are admins last 2 are employees
        List<Account> sampleAccounts = Util.buildAccounts();
        Account admin1 = sampleAccounts.get(0);
        Account employee1 = sampleAccounts.get(2);

        return Stream.of(
                Arguments.of(admin1, HttpStatus.CREATED.value(), postRequestBody), // create admin using admin account
                Arguments.of(employee1, HttpStatus.UNAUTHORIZED.value(), postRequestBody)// create admin using employee account.
        );
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
    void adminShouldGetAdminByUuid(){
        /*
         * tests that admin can get admin by uuid.
         * checks that the response status code is 200 OK.
         * checks that it returns the correct admin.
         * */

        // sampleAccounts: list of 4 account first 2 accounts are admins, last 2 account are employees
        Account admin1 = sampleAccounts.get(0);
        Account admin2 = sampleAccounts.get(1);
        // authenticate with account with role employee.
        String accessToken = util.attemptAuthenticationWith(admin1);
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(apiUrl + "/" + admin2.getAccountUuid())
                .then()
                .statusCode(200)
                .body("name", equalTo(admin2.getName()))
                .body("email", equalTo(admin2.getEmail()))
                .body("accountUuid", equalTo(admin2.getAccountUuid().toString()));
    }
    @Test
    void employeeShouldNotGetAdminByUuid(){
        /*
         * tests that employee can't get admin by uuid.
         * checks that the response status code is 401 unauthorized.
         * */

        // sampleAccounts: list of 4 account first 2 accounts are admins, last 2 account are employees
        Account employee = sampleAccounts.get(2);
        Account admin = sampleAccounts.get(0);
        // authenticate with account with role employee.
        String accessToken = util.attemptAuthenticationWith(employee);
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(apiUrl + "/" + admin.getAccountUuid())
                .then()
                .statusCode(401);
    }

    @ParameterizedTest
    @MethodSource("provideAccountsForAddAdminWithInValidDataTest")
    void shouldNotAddAdminWithInValidData(Account account, int expectedStatusCode,
                                          Map<String, Object> requestBody){
        /*
         * account: is the account which will attempt to create new admin account.
         * expectedStatusCode: is the status code that should be returned with the response.
         * requestBody: contains the attributes of the new account.
         * */

        // endpoint to which the new account creation request will be sent to.
        final String endpoint = "/api/admin";

        // attempt authentication and get an access token.
        String accessToken = util.attemptAuthenticationWith(account);

        // send post request to endpoint "/api/admin" with a request body containing the new admin account
        // attributes, along with a bearer token.
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken).body(requestBody)
                .when()
                .request(HttpMethod.POST.toString(), endpoint)
                .then()
                .statusCode(expectedStatusCode);

    }
    static Stream<Arguments> provideAccountsForAddAdminWithInValidDataTest(){

        Map.Entry<String, Object> email = Map.entry("email", "newadmin@email.com");
        Map.Entry<String, Object> password = Map.entry("password", "password123");
        Map.Entry<String, Object> role = Map.entry("role", Role.ADMIN);
        Map.Entry<String, Object> name = Map.entry("name", "New Admin");

        // returns a list of 4 accounts first 2 are admins last 2 are employees
        List<Account> sampleAccounts = Util.buildAccounts();
        Account admin = sampleAccounts.get(0);
        return Stream.of(
                Arguments.of(admin, HttpStatus.BAD_REQUEST.value(), Map.ofEntries(password, role, name)), // create admin without email
                Arguments.of(admin, HttpStatus.BAD_REQUEST.value(),  Map.ofEntries(email,role, name)) , // create admin without password
                Arguments.of(admin, HttpStatus.BAD_REQUEST.value(),  Map.ofEntries(email, password, name)) // create admin without role.
        );
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

}