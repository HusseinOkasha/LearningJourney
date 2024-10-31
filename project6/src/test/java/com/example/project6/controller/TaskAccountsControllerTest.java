package com.example.project6.controller;

import com.example.project6.Service.AccountService;
import com.example.project6.Service.AccountTasksService;
import com.example.project6.Service.TaskAccountsService;
import com.example.project6.Service.TaskService;
import com.example.project6.Util;
import com.example.project6.dto.ProfileDto;
import com.example.project6.dto.TaskAccountDto;
import com.example.project6.dto.TaskDto;
import com.example.project6.entity.Account;
import com.example.project6.entity.AccountTaskLink;
import com.example.project6.entity.Task;
import com.example.project6.entity.TaskAccountLink;
import com.example.project6.util.entityAndDtoMappers.TaskMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// to make sure that spring boot doesn't override the configuration and creates h2 database or any other in memory
// database.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class TaskAccountsControllerTest {

    // hold the port on which the app will be listening.
    @LocalServerPort
    private Integer port;

    // dependencies
    private final DynamoDbClient dynamoDbClient;
    private final AccountService accountService;
    private final TaskService taskService;
    private final AccountTasksService accountTasksService;

    // constructor
    @Autowired
    public TaskAccountsControllerTest(DynamoDbClient dynamoDbClient,
                                      AccountService accountService, TaskService taskService,
                                      AccountTasksService accountTasksService,
                                      TaskAccountsService taskAccountsService, Util util) {
        this.dynamoDbClient = dynamoDbClient;
        this.accountService = accountService;
        this.taskService = taskService;
        this.accountTasksService = accountTasksService;
        this.taskAccountsService = taskAccountsService;
        this.util = util;
    }

    private final TaskAccountsService taskAccountsService;
    private final Util util;

    // holds sample data we will use in our tests.
    private List<Account> sampleAccounts;
    private List<Task> sampleTasks;

    // endpoint for task controller.
    static final String API_URL = "/api/task";

    // container in which holds dynamoDB.
    private static LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack" +
            "/localstack:3.2"))
            .withServices(LocalStackContainer.Service.DYNAMODB);

    @BeforeAll
    static void beforeAll() {
        // starts local stack container.
        localStack.start();

        // extract the random port number on which dynamodb listens.
        System.setProperty("amazon.dynamodb.endpoint",
                localStack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString());
    }

    @BeforeEach
    void setUp() {
        localStack.start();
        System.setProperty("amazon.dynamodb.endpoint",
                localStack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString());


        RestAssured.baseURI = "http://localhost:" + port;
        dynamoDbClient.createTable(util.buildCreateTableRequest());

        // returns a list of 4 account first 2 accounts are admins, last 2 account are employees
        sampleAccounts = Util.buildAccounts();

        // create a list of 2 tasks.
        sampleTasks = Util.buildSampleTasks();

        // save sample accounts to the database.
        sampleAccounts.forEach(accountService::save);

        // save sample tasks to the database.
        sampleTasks.forEach(taskService::save);

        /*
         * Build the following links:
         *   - link admin1 --> task1
         *   - link employee1 --> task1
         *   - link task1  --> admin1
         *   - link task1 --> employee1
         * */
        AccountTaskLink admin1TaskLink = Util.buildAccountTaskLinkWith(sampleAccounts.get(0), sampleTasks.get(0));
        TaskAccountLink taskAdmin1Link = Util.buildTaskAccountLinkWith(sampleAccounts.get(0), sampleTasks.get(0));
        AccountTaskLink employee1TaskLink = Util.buildAccountTaskLinkWith(sampleAccounts.get(2), sampleTasks.get(0));
        TaskAccountLink taskEmployee1Link = Util.buildTaskAccountLinkWith(sampleAccounts.get(2), sampleTasks.get(0));

        /*
         * Build the following links:
         *   - link admin2 --> task2
         *   - link employee2 --> task2
         *   - link task2  --> admin2
         *   - link task2 --> employee2
         * */
        AccountTaskLink admin2TaskLink = Util.buildAccountTaskLinkWith(sampleAccounts.get(1), sampleTasks.get(1));
        TaskAccountLink taskAdmin2Link = Util.buildTaskAccountLinkWith(sampleAccounts.get(1), sampleTasks.get(1));
        AccountTaskLink employee2TaskLink = Util.buildAccountTaskLinkWith(sampleAccounts.get(3), sampleTasks.get(1));
        TaskAccountLink taskEmployee2Link = Util.buildTaskAccountLinkWith(sampleAccounts.get(3), sampleTasks.get(1));

        // save accountTask links to the database.
        List<AccountTaskLink> accountTaskLinks = List.of(admin1TaskLink, employee1TaskLink,
                admin2TaskLink, employee2TaskLink);
        accountTaskLinks.forEach(accountTasksService::save);

        // save task account links to the database.
        List<TaskAccountLink> taskAccountLinks = List.of(taskAdmin1Link, taskEmployee1Link,
                taskAdmin2Link, taskEmployee2Link);
        taskAccountLinks.forEach(taskAccountsService::save);

    }

    @AfterEach
    void tearDown() {
        dynamoDbClient.deleteTable(DeleteTableRequest.builder().tableName("app").build());
    }

    /*
    * get account tasks tests.
    * */
    @Test
    void shouldGetAccountTasks() {
        /*
         * verifies that :
         *   - admin / employee with whom the task is shared,
         *       can list all the accounts with whom the task is shared
         *   - list of returned task account dtos has the .
         *   - response status code is 200 OK.
         * */

        // get admin account.
        Account admin = sampleAccounts.get(0);

        // get employee account.
        Account employee = sampleAccounts.get(2);

        shouldGetAccountTasksAs(admin);
        shouldGetAccountTasksAs(employee);
    }

    private void shouldGetAccountTasksAs(Account account) {
        // authenticate as account.
        String accessToken = util.attemptAuthenticationWith(account);

        // get task
        // note that this task  is shared with sampleAccounts.get(0) and sampleAccounts.get(2).
        Task task = sampleTasks.get(0);

        // create task account dtos.
        // note that the specified accounts from sample accounts are already linked to previous task, look at the setup method.
        List<TaskAccountDto> expectedTaskAccountDtos = List.of(
                new TaskAccountDto(
                        sampleAccounts.get(0).getAccountUuid(),
                        sampleAccounts.get(0).getName(),
                        task.getTaskUuid(),
                        task.getTitle()
                ),
                new TaskAccountDto(
                        sampleAccounts.get(2).getAccountUuid(),
                        sampleAccounts.get(2).getName(),
                        task.getTaskUuid(),
                        task.getTitle()
                )
        );

        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(String.format("%s/%s/accounts", API_URL, task.getTaskUuid()));

        // extract the returned taskDto
        Set<TaskAccountDto> taskAccountDtos = Set.copyOf(
                response.jsonPath().getList("", TaskAccountDto.class)
        );

        // check that the response status code is 200 OK.
        response.then().statusCode(HttpStatus.OK.value());

        // check that all the returned task account dtos are the same as the expected ones.
        taskAccountDtos.forEach(taskAccountDto -> assertThat(taskAccountDto).isIn(expectedTaskAccountDtos));
        expectedTaskAccountDtos.forEach(expectedTaskAccountDto -> assertThat(expectedTaskAccountDto).isIn(taskAccountDtos));
    }

    @Test
    void shouldNotGetTaskAccounts(){

        /*
        * Verifies that:
        *   - employee with whom the task isn't shared,
        *   - can get the task accounts.
        *   - response status code is 404 NOT_FOUND.
        *   - error message is same as expected one.
        * */

        // get task
        // note that this task  is shared with sampleAccounts.get(0) and sampleAccounts.get(2).
        Task task = sampleTasks.get(0);

        // get employee account with whom the task isn't shared
        Account employee = sampleAccounts.get(3);

        // authenticate as account.
        String accessToken = util.attemptAuthenticationWith(employee);

        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(String.format("%s/%s/accounts", API_URL, task.getTaskUuid()));

        response
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        Map<String, String> error = response.getBody().as(Map.class);
        Map<String, String> expectedError = Map.of(
                "error", String.format("couldn't find task with uuid: %s that belongs to account with uuid: %s",
                        task.getTaskUuid(), employee.getAccountUuid()
                )
        );

        // check that the returned taskDto is the expected one.
        assertThat(error).isEqualTo(expectedError);
    }

    /*
    * Share task with account tests.
    * */
    @Test
    void shouldShareTaskWithAccountAsAdmin(){
        /*
        * It verifies that:
        *   - admin can share a task with other accounts.
        *   - checks that response status code is 200 OK.
        * */

        // get sample account.
        Account admin  = sampleAccounts.get(0);

        // get sample task.
        Task task = sampleTasks.get(0);

        // authenticate with admin account.
        String accessToken =  util.attemptAuthenticationWith(admin);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .post(String.format("%s/%s/accounts/%s", API_URL, task.getTaskUuid(), admin.getAccountUuid()))
                .then()
                .statusCode(HttpStatus.OK.value());

    }

}