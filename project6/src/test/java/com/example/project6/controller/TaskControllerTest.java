package com.example.project6.controller;

import com.example.project6.Enum.TaskStatus;
import com.example.project6.Service.AccountService;
import com.example.project6.Service.AccountTasksService;
import com.example.project6.Service.TaskAccountsService;
import com.example.project6.Service.TaskService;
import com.example.project6.Util;
import com.example.project6.dto.TaskDto;
import com.example.project6.entity.Account;
import com.example.project6.entity.AccountTaskLink;
import com.example.project6.entity.Task;
import com.example.project6.entity.TaskAccountLink;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.compress.archivers.EntryStreamOffsets;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ser.std.MapProperty;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// to make sure that spring boot doesn't override the configuration and creates h2 database or any other in memory
// database.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class TaskControllerTest {
    @LocalServerPort
    private Integer port;

    private final DynamoDbClient dynamoDbClient;
    private final AccountService accountService;
    private final TaskService taskService;
    private final AccountTasksService accountTasksService;
    private final TaskAccountsService taskAccountsService;
    private final Util util;
    private List<Account> sampleAccounts;
    private List<Task> sampleTasks;
    private static LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.2"))
            .withServices(LocalStackContainer.Service.DYNAMODB);
    static final String API_URL = "/api/task";

    @Autowired
    TaskControllerTest(DynamoDbClient dynamoDbClient, AccountService accountService, TaskService taskService, AccountTasksService accountTasksService, TaskAccountsService taskAccountsService, Util util) {
        this.dynamoDbClient = dynamoDbClient;
        this.accountService = accountService;
        this.taskService = taskService;
        this.accountTasksService = accountTasksService;
        this.taskAccountsService = taskAccountsService;
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
    void afterEach(){
        dynamoDbClient.deleteTable(DeleteTableRequest.builder().tableName("app").build());
    }

    @Test
    void shouldCreateNewTaskAsAdmin(){
        /*
        * checks that an account with role ADMIN can create new tasks.
        * It checks that:
        *   - The response status code is 201 CREATED.
        *   - The returned taskDto is the expected one.
        * */

        // get admin account.
        Account admin = sampleAccounts.get(0);

        // authenticate with the fetched admin account.
        String accessToken  = util.attemptAuthenticationWith(admin);

        // create the request body.
        TaskDto expectedTaskDto =  new TaskDto("start banking system project",
                "is an e-financial solution",
                TaskStatus.DONE, null);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", expectedTaskDto.title());
        requestBody.put("description",expectedTaskDto.description());
        requestBody.put("status", expectedTaskDto.taskStatus());

        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .post(API_URL);

        response.then().statusCode(HttpStatus.CREATED.value()); // check that the status code is 201 CREATED.

        TaskDto taskDto = response.getBody().as(TaskDto.class);

        // check that the expectedTaskDto is the same as the expected one.
        assertThat(taskDto)
                .usingRecursiveComparison()
                // exclude the taskUuid as it is generated just before saving the task to the database.
                .ignoringFields("taskUuid")
                .isEqualTo(expectedTaskDto);
    }
    @ParameterizedTest
    @MethodSource("provideTasksForTestingInValidTaskCreation")
    void shouldNotCreateTaskWithInValidData(Map<String,Object>requestBody, Map<String, String> expectedErrors){
        /*
        * It checks that we can't create task with invalid data.
        * In valid data:
        *   - empty / null description.
        *   - empty / null title.
        * */
        Account admin = sampleAccounts.get(0);
        String accessToken = util.attemptAuthenticationWith(admin);

        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .post(API_URL);

        Map errors = response.getBody().as(Map.class);
        response.then().statusCode(HttpStatus.BAD_REQUEST.value());
        assertThat(errors).isEqualTo(expectedErrors);
    }

    static Stream<Arguments> provideTasksForTestingInValidTaskCreation(){
        /*
        * Creates invalid tasks:
        *   - with empty / null description
        *   - with empty / null title
        *   - with empty / null status.
        * returns stream of arguments each argument contains:
        *   - Task to be created.
        *   - expected status code.
        *   - expected error message.
        * */

        String inValidDescriptionErrorMessage = "description shouldn't be empty nor blank";
        String inValidTitleErrorMessage = "title shouldn't be empty nor blank";
        String inValidStatusErrorMessage = "Invalid value provided for TaskStatus. Expected values are: TODO, IN_PROGRESS, DONE.";

        return Stream.of(
                // with empty description.
                Arguments.of(
                        Map.of(
                                "description","",
                                "title", "new task title",
                                "status", TaskStatus.TODO
                        ),Map.of("description", inValidDescriptionErrorMessage)
                ),

                // with null description
                Arguments.of(
                        Map.of(
                                "title", "new task title",
                                "status", TaskStatus.TODO
                        ),
                        Map.of("description", inValidDescriptionErrorMessage)),

                // with empty title.
                Arguments.of(
                        Map.of(
                                "description","new task description",
                                "title", "",
                                "status", TaskStatus.TODO
                        ),
                        Map.of("title", inValidTitleErrorMessage)

                ),
                // with null title.
                Arguments.of(
                        Map.of(
                                "description","new task description",
                                "status", TaskStatus.TODO
                        ),
                        Map.of("title", inValidTitleErrorMessage)
                ),
                // with empty status.
                Arguments.of(
                        Map.of(
                                "description","new task description",
                                "title", "new task title",
                                "status", ""
                        ),
                        Map.of("status", inValidStatusErrorMessage)
                ),
                // without status.
                Arguments.of(
                        Map.of(
                                "description","new task description",
                                "title", "new task title"
                        ),
                        Map.of("status", "must not be null")
                ),
                // with invalid status.
                Arguments.of(
                        Map.of(
                                "description","new task description",
                                "title", "new task title",
                                "status", "randomString"
                        ),
                        Map.of("status", inValidStatusErrorMessage)
                )

        );



    }

}