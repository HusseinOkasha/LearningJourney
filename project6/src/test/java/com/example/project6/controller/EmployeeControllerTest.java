package com.example.project6.controller;

import com.example.project6.Enum.Role;
import com.example.project6.Service.AccountService;
import com.example.project6.Util;
import com.example.project6.dto.ProfileDto;
import com.example.project6.entity.Account;
import com.example.project6.util.entityAndDtoMappers.AccountMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
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

import java.util.*;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

import static org.assertj.core.api.Assertions.assertThat;

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
    static void beforeAll() {
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


    /*
     * Add employee tests.
     * */
    @Test
    void shouldAddEmployeeAsAdmin() {
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
                                             Map<String, Object> requestBody) {
        /*
         * account: is the account which will attempt to create new admin account.
         * expectedStatusCode: is the status code that should be returned with the response.
         * requestBody: contains the attributes of the new account.
         * */


        // attempt authentication and get an access token.
        String accessToken = util.attemptAuthenticationWith(account);

        // send post request to endpoint "/api/admin/employees" with a request body containing the new admin account
        // attributes, along with a bearer token.
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken).body(requestBody)
                .when()
                .request(HttpMethod.POST.toString(), API_URL)
                .then()
                .statusCode(expectedStatusCode);

    }

    static Stream<Arguments> provideAccountsForAddEmployeeWithInValidDataTest() {

        Map.Entry<String, Object> email = Map.entry("email", "newEmployee@email.com");
        Map.Entry<String, Object> password = Map.entry("password", "password123");
        Map.Entry<String, Object> role = Map.entry("role", Role.EMPLOYEE);
        Map.Entry<String, Object> name = Map.entry("name", "New employee");

        // returns a list of 4 accounts first 2 are admins last 2 are employees
        List<Account> sampleAccounts = Util.buildAccounts();
        Account admin = sampleAccounts.get(0);
        return Stream.of(
                Arguments.of(admin, HttpStatus.BAD_REQUEST.value(), Map.ofEntries(password, role, name)), // create
                // employee without email
                Arguments.of(admin, HttpStatus.BAD_REQUEST.value(), Map.ofEntries(email, role, name)), // create
                // employee without password
                Arguments.of(admin, HttpStatus.BAD_REQUEST.value(), Map.ofEntries(email, password, name)) // create
                // employee without role.
        );
    }

    /*
     * Get employee by uuid tests.
     * */
    @Test
    void shouldGetEmployeeByUuidAsAdmin() {
        /*
         * Tests that account of role ADMIN can get employee by uuid.
         * It checks that:
         *   - it returns the expected employee.
         *   - response status code is 200 OK.
         *
         * */
        // get sample admin account.
        Account admin = sampleAccounts.get(0);

        // get sample employee.
        Account expectedEmployee = sampleAccounts.get(2);

        // attempt authentication and get an access token.
        String accessToken = util.attemptAuthenticationWith(admin);

        // send get request to endpoint "/api/admin/employees/employeeUuid",
        // with a request body containing the new admin account

        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(String.format("%s/%s", API_URL, expectedEmployee.getAccountUuid()));

        // extract the response body.
        ProfileDto employeeProfile = response.getBody().as(ProfileDto.class);

        // check that the returned employee is the expected one.
        assertThat(employeeProfile).isEqualTo(AccountMapper.AccountEntityToAccountProfileDto(expectedEmployee));

        // check that the response status code is 200 OK.
        response.then()
                .statusCode(HttpStatus.OK.value());
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForGettingOrDeletingEmployeeWithInvalidUuid")
    void shouldNotGetOrDeleteEmployeeByInValidUuid(Account admin, HttpStatus expectedStatusCode,
                                                        HttpMethod method, String endpoint) {
        /*
         * Arguments:
         *   1) admin:
         *       - account that will try to get employee by uuid.
         *   2) expectedStatusCode
         *       - the status code that should be returned with the request.
         *   3) method
         *       - The http method that we should use when sending the request.
         *   4) endpoint
         *       - is the endpoint that we will try to send the request to.
         * */

        /*
         * Tests that admin can't get employee by invalid uuid.
         * Invalid uuid:
         *   - NonExistent uuid. ==> checks that the response status code is 404 NOT_FOUND.
         *   - String that isn't uuid. ==> checks that the response status code is 400 BAD_REQUEST.
         *
         * Tests that deleting an employee given that:
         *   - You are using admin account.
         *   - and a random string that it isn't uuid. ==> checks that response status code is 400 BAD_REQUEST.
         * */

        // attempt authentication and get an access token.
        String accessToken = util.attemptAuthenticationWith(admin);

        // send get request to endpoint "/api/admin/employees/employeeUuid" with a request body containing the new
        // admin account
        // attributes, along with a bearer token.
        RequestSpecification request = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken);
        request.request(method.toString(), endpoint)
                .then().statusCode(expectedStatusCode.value());

    }

    static Stream<Arguments> provideArgumentsForGettingOrDeletingEmployeeWithInvalidUuid() {
        /*
         * Provide the arguments for testing:
         *   - getting employee with nonExistent uuid.
         *   - getting employee using malformed uuid.
         *   - deleting employee using malformed uuid.
         * */

        // returns a list of 4 accounts first 2 are admins last 2 are employees
        List<Account> sampleAccounts = Util.buildAccounts();

        // get the admin that will send the requests.
        Account admin = sampleAccounts.get(0);
        String nonExistentUuid = UUID.randomUUID().toString();
        String inValidUuid = "7amsa"; // just random string that is not  uuid.

        return Stream.of(
                // get employee with nonExistent uuid.
                Arguments.of(admin, HttpStatus.NOT_FOUND,
                        HttpMethod.GET, String.format("%s/%s", API_URL, nonExistentUuid)
                ),

                // get employee by invalid uuid.
                Arguments.of(admin, HttpStatus.BAD_REQUEST,
                        HttpMethod.GET, String.format("%s/%s", API_URL, inValidUuid)
                ),

                // delete employee with inValid uuid
                Arguments.of(admin, HttpStatus.BAD_REQUEST,
                        HttpMethod.DELETE, String.format("%s/%s", API_URL, inValidUuid)
                )
        );
    }


    /*
     * Get all employees tests.
     * */
    @Test
    void shouldGetAllEmployeesAsAdmin() {
        /*
         * Tests that account of role ADMIN can get all employees, it checks that.
         *   - The number of the returned employees is as expected.
         *   - The returned employees are the same as expected ones.
         *   - With response status code 200 OK.
         * */

        // sample accounts contains 2 admin accounts the first 2 accounts, and 2 employee accounts the last 2 accounts.
        Account admin = sampleAccounts.get(0);

        // convert the expected employees to profile dto.
        List<ProfileDto> expectedEmployees = Stream.of(sampleAccounts.get(2), sampleAccounts.get(3))
                .map(AccountMapper::AccountEntityToAccountProfileDto)
                .toList();

        // attempt authentication and get an access token.
        String accessToken = util.attemptAuthenticationWith(admin);

        // send get request to endpoint "/api/admin/employees/all" with a request body containing the new admin account
        // attributes, along with a bearer token.
        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .get(API_URL + "/all");

        // extract the returned employees
        Set<ProfileDto> employees = Set.copyOf(response.jsonPath().getList("", ProfileDto.class));

        // check that the response status code is 200 OK.
        response.then().statusCode(HttpStatus.OK.value());

        // check that the number of the returned employees is as expected.
        assertThat(employees.size()).isEqualTo(expectedEmployees.size());

        // check that the returned employees are the expected employees
        employees.forEach(employee -> assertThat(employee).isIn(expectedEmployees));
    }

    /*
     * Delete employee by uuid tests.
     * */
    @Test
    void shouldDeleteEmployeeByUuidAsAdmin() {
        /*
         * Tests that admin can delete employee by uuid.
         * it checks that:
         *   - response status code is 200 ok.
         *   - checks that the employee is actually deleted.
         * */

        // get sample admin account.
        Account admin = sampleAccounts.get(0);

        // account to be deleted.
        Account employee = sampleAccounts.get(2);

        // attempt authentication and get an access token.
        String accessToken = util.attemptAuthenticationWith(admin);

        // send delete request to endpoint "/api/admin/employees/employeeUuid"
        // attributes, along with a bearer token.
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .delete(String.format("%s/%s", API_URL, employee.getAccountUuid()))
                .then().statusCode(HttpStatus.OK.value()); // check that the status code is 200 OK.

        // send get request to endpoint "/api/admin/employees/employeeUuid",
        // and check that the response status code 404 NOT_FOUND.
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(String.format("%s/%s", API_URL, employee.getAccountUuid()))
                .then().statusCode(HttpStatus.NOT_FOUND.value()); // check that the status code is 404 NOT_FOUND.

    }

    /*
     * tests for employee authorization on employee controller.
     * */
    @ParameterizedTest
    @MethodSource("provideArgumentsForEmployeeAuthorizationOnEmployeeController")
    void testEmployeeAuthorizationOnEmployeeController(Account employee, HttpStatus expectedStatusCode,
                                                       Map<String, Object> requestBody, HttpMethod method,
                                                       String endPoint) {
        /*
         * Arguments:
         *   1) Employee
         *       - the employee which will try to consume the employee controller endpoints.
         *   2) expectedHttpStatusCode
         *       - is the status code that should be returned with the response.
         *   3) requestBody
         *       - contains the attributes of the employee to be created, otherwise it's empty.
         *   4) method
         *       - represents the http method of the request to be sent.
         *   5) endpoint
         *       - represents the endPoint to which the request should be sent.
         * */

        /*
         * This method tests that account of role EMPLOYEE can't:
         *   - create new employee account.
         *   - get employee by account uuid.
         *   - get all employees.
         *   - delete employee by uuid.
         *
         * It checks that the response status code is 401 UNAUTHORIZED.
         * */

        // attempt authentication with the provided account.
        // it will return an access token the result of this authentication process.
        String accessToken = util.attemptAuthenticationWith(employee);

        RequestSpecification request = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken);

        // set the request body in case it exists.
        if (!requestBody.isEmpty()) {
            request = request.body(requestBody);
        }
        request
                .when()
                .request(method.toString(), endPoint)
                .then()
                .statusCode(expectedStatusCode.value());
    }

    static Stream<Arguments> provideArgumentsForEmployeeAuthorizationOnEmployeeController() {
        // create a request body containing the employee to be created.
        Map.Entry<String, Object> email = Map.entry("email", "newEmployee@email.com");
        Map.Entry<String, Object> password = Map.entry("password", "password123");
        Map.Entry<String, Object> role = Map.entry("role", Role.EMPLOYEE);
        Map.Entry<String, Object> name = Map.entry("name", "New employee");

        // returns a list of 4 accounts first 2 are admins last 2 are employees
        List<Account> sampleAccounts = Util.buildAccounts();

        // get the employee that will send the requests.
        Account employee1 = sampleAccounts.get(2);
        Account employee2 = sampleAccounts.get(3);

        // endpoints
        final String createNewEmployeeUrl = API_URL;
        final String getEmployeeByUuidUrl = String.format("%s/%s", API_URL, employee2.getAccountUuid());
        final String getAllEmployeesUrl = String.format("%s/all", API_URL);
        final String deleteEmployeeByUuidUrl = String.format("%s/%s", API_URL, employee2.getAccountUuid());
        return Stream.of(
                // create new employee.
                Arguments.of(employee1, HttpStatus.UNAUTHORIZED, Map.ofEntries(password, role, name),
                        HttpMethod.POST, createNewEmployeeUrl
                ),

                // get employee by uuid.
                Arguments.of(employee1, HttpStatus.UNAUTHORIZED, Map.of(),
                        HttpMethod.GET, getEmployeeByUuidUrl
                ),

                // get all employees.
                Arguments.of(employee1, HttpStatus.UNAUTHORIZED, Map.of(),
                        HttpMethod.GET, getAllEmployeesUrl),

                // Delete employee by uuid.
                Arguments.of(employee1, HttpStatus.UNAUTHORIZED, Map.of(),
                        HttpMethod.GET, deleteEmployeeByUuidUrl)

        );
    }
}