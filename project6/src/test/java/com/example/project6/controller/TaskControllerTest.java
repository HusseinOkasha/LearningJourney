package com.example.project6.controller;

import com.example.project6.Enum.TaskStatus;
import com.example.project6.Service.*;
import com.example.project6.Util;
import com.example.project6.dto.TaskDto;
import com.example.project6.entity.Account;
import com.example.project6.entity.AccountTaskLink;
import com.example.project6.entity.Task;
import com.example.project6.entity.TaskAccountLink;
import com.example.project6.util.entityAndDtoMappers.TaskMapper;
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

import java.util.*;
import java.util.stream.Collectors;
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

    // hold the port on which the app will be listening.
    @LocalServerPort
    private Integer port;

    // dependencies
    private final DynamoDbClient dynamoDbClient;
    private final AccountService accountService;
    private final TaskService taskService;
    private final AccountTasksService accountTasksService;
    private final TaskAccountsService taskAccountsService;
    private final Util util;

    // holds sample data we will use in our tests.
    private List<Account> sampleAccounts;
    private List<Task> sampleTasks;

    // expected error messages in case of invalid data.
    private final static Map<String,  String> inValidDescriptionErrorMessage = Map.of(
            "description","description shouldn't be empty nor blank");
    private final static Map<String, String> inValidTitleErrorMessage = Map.of(
            "title", "title shouldn't be empty nor blank");
    private final static Map<String, String> inValidStatusErrorMessage = Map.of(
            "status", "Invalid value provided for TaskStatus. Expected values are: TODO, IN_PROGRESS, DONE."
    );

    // endpoint for task controller.
    static final String API_URL = "/api/task";

    // container in which holds dynamoDB.
    private static LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack" +
            "/localstack:3.2"))
            .withServices(LocalStackContainer.Service.DYNAMODB);

    // constructor.
    @Autowired
    TaskControllerTest(DynamoDbClient dynamoDbClient, AccountService accountService, TaskService taskService,
                       AccountTasksService accountTasksService, TaskAccountsService taskAccountsService, Util util) {
        this.dynamoDbClient = dynamoDbClient;
        this.accountService = accountService;
        this.taskService = taskService;
        this.accountTasksService = accountTasksService;
        this.taskAccountsService = taskAccountsService;
        this.util = util;
    }

    @BeforeAll
    static void beforeAll() {
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
    void afterEach() {
        dynamoDbClient.deleteTable(DeleteTableRequest.builder().tableName("app").build());
    }

    @Test
    void shouldCreateNewTaskAsAdmin() {
        /*
         * checks that an account with role ADMIN can create new tasks.
         * It checks that:
         *   - The response status code is 201 CREATED.
         *   - The returned taskDto is the expected one.
         * */

        // get admin account.
        Account admin = sampleAccounts.get(0);

        // authenticate with the fetched admin account.
        String accessToken = util.attemptAuthenticationWith(admin);

        // create the request body.
        TaskDto expectedTaskDto = new TaskDto("start banking system project",
                "is an e-financial solution",
                TaskStatus.DONE, null);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", expectedTaskDto.title());
        requestBody.put("description", expectedTaskDto.description());
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
    @MethodSource("provideInvalidTasksAndErrorMessages")
    void shouldNotCreateTaskWithInValidData(Map<String, Object> requestBody, Map<String, String> expectedErrors) {
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

    @Test
    void shouldGetTaskByUuidAsAdmin() {
        /*
         * tests that account of role ADMIN can get task by uuid.
         * It checks that:
         *   - response status code is: 200 OK.
         *   - the returned task dto is the expected one.
         * */

        // get admin account.
        Account admin = sampleAccounts.get(0);
        String accessToken = util.attemptAuthenticationWith(admin);

        // get sample task.
        Task task = sampleTasks.get(0);

        // create task dto from the sample task.
        TaskDto expectedTaskDto = TaskMapper.TaskEntityToTaskDto(task);

        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(String.format("%s/%s", API_URL, task.getTaskUuid()));

        // extract the returned taskDto
        TaskDto taskDto = response.getBody().as(TaskDto.class);

        // check that the response status code is 200 OK.
        response.then().statusCode(HttpStatus.OK.value());

        // check that the returned taskDto is the expected one.
        assertThat(taskDto).isEqualTo(expectedTaskDto);
    }

    @ParameterizedTest
    @MethodSource("provideUuidsTestingGettingTaskWithInvalidUuid")
    void shouldNotGetTaskWithInvalidUUID(String taskUuid, HttpStatus expectedStatusCode) {
        /*
         * InValid uuid means:
         *   - no task with this uuid.
         *   - malformed uuid.
         * */

        // get sample admin account.
        Account admin = sampleAccounts.get(0);
        String accessToken = util.attemptAuthenticationWith(admin);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(String.format("%s/%s", API_URL, taskUuid))
                .then()
                .statusCode(expectedStatusCode.value());


    }

    @Test
    void shouldUpdateTaskByUuidAsAdmin() {
        /*
         * checks the following:
         *   - account with role admin can update task by its uuid:
         *   - checks that the response status code is 200 OK.
         *   - checks that the returned taskDto is updated.
         * */

        // get sample account.
        Account admin = sampleAccounts.get(0);
        String accessToken = util.attemptAuthenticationWith(admin);

        // get sample task
        Task task = sampleTasks.get(0);

        // perform updates on the task
        task = Util.updateTask(task);

        // create taskDto from the task after the update.
        TaskDto expectedTaskDto = TaskMapper.TaskEntityToTaskDto(task);

        // build request body from the updated task.
        Map<String, Object> requestBody = Util.buildUpdateTaskRequestBody(task);

        // send the request.
        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .body(requestBody)
                .put(String.format("%s/%s", API_URL, task.getTaskUuid()));

        // check that the response status code is as expected.
        response.then()
                .statusCode(HttpStatus.OK.value());

        // extract the updated taskDto returned with the request.
        TaskDto taskDto = response.getBody().as(TaskDto.class);

        // check that the returned taskDto is equal to the expected one.
        assertThat(taskDto).isEqualTo(expectedTaskDto);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidTasksAndErrorMessages")
    void shouldNotUpdateTaskByUuidWithInValidData(Map<String, Object> requestBody, Map<String, String> expectedErrors) {
        /*
         * checks that you can't update task with inValid data.
         * Invalid data means:
         *   - Invalid taskDto.
         *       - empty / null description.
         *       - empty / null title.
         *       - empty / null / malformed status.
         *   - Invalid uuid.
         *       - uuid that doesn't correspond to any task.
         *       - random string that isn't uuid.
         * */

        // get sample account with role
        Account admin = sampleAccounts.get(0);

        // get sample task.
        Task task = sampleTasks.get(0);
        String accessToken = util.attemptAuthenticationWith(admin);

        // send the request.
        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .body(requestBody)
                .put(String.format("%s/%s", API_URL, task.getTaskUuid()));

        response.then().statusCode(HttpStatus.BAD_REQUEST.value());

        Map<String, String> errors = response.getBody().as(Map.class);

        assertThat(errors).isEqualTo(expectedErrors);
    }

    @Test
    void shouldUpdateTaskTitleByUuidAsAdmin() {
        /*
         * checks that:
         *   - account with role admin can update the tasks title.
         *   - with response status code 200 OK.
         *   - And the returned taskDto(in the response body) is the same as the expected taskDto.
         * */

        // get admin account.
        Account admin = sampleAccounts.get(0);

        // get sample task.
        Task task = sampleTasks.get(0);

        String accessToken = util.attemptAuthenticationWith(admin);

        //update the task title.
        task.setTitle("updateTile");

        // create taskDto to be the ground truth.
        TaskDto expectedTaskDto = TaskMapper.TaskEntityToTaskDto(task);

        // build the request body.
        Map<String, String> requestBody = Map.of("title", "updateTile");

        // send the request.
        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .body(requestBody)
                .patch(String.format("%s/%s/title", API_URL, task.getTaskUuid()));

        // check that the response status code is as expected.
        response.then()
                .statusCode(HttpStatus.OK.value());

        // extract the updated taskDto returned with the request.
        TaskDto taskDto = response.getBody().as(TaskDto.class);

        // check that the returned taskDto is equal to the expected one.
        assertThat(taskDto).isEqualTo(expectedTaskDto);

    }

    static Stream<Arguments> provideUuidsTestingGettingTaskWithInvalidUuid() {
        return Stream.of(Arguments.of(UUID.randomUUID().toString(), HttpStatus.NOT_FOUND),
                Arguments.of("randomString", HttpStatus.BAD_REQUEST));
    }

    static Stream<Arguments> provideInvalidTasksAndErrorMessages() {
        /*
         * Creates invalid tasks:
         *   - with empty / null description
         *   - with empty / null title
         *   - with empty / null status.
         * returns stream of arguments each argument contains:
         *   - Task to be created.
         *   - expected error message.
         * */

        List<String>invalidDescriptions = Util.getInvalidTaskDescriptions();
        List<String>invalidTitles = Util.getInvalidTaskTitles();
        List<String>invalidStatus = Util.getInvalidTaskStatus();

        Map<String, String> validRequestBody = Map.of(
                "description", "new description",
                "title", "new task title",
                "status", TaskStatus.TODO.toString()
        );

        // initialize list of arguments.
        List<Arguments> arguments;

        // create arguments with invalid description.
        arguments = invalidDescriptions.stream().map(
                description->{
                  Map<String, String> inValidRequestBody = Util // update the description with the invalid value.
                          .updateRequestBody(validRequestBody, Map.of("description", description), List.of());
                  return Arguments.of(inValidRequestBody, inValidDescriptionErrorMessage);
                }
        ).collect(Collectors.toList());

        // create arguments with invalid title.
        arguments.addAll(invalidTitles.stream().map(
                title->{
                    Map<String, String> inValidRequestBody = Util // update title with the invalid value.
                            .updateRequestBody(validRequestBody, Map.of("title", title), List.of());
                    return Arguments.of(inValidRequestBody, inValidTitleErrorMessage );
                }
        ).toList());

        // create arguments with invalid status.
        arguments.addAll(invalidStatus.stream().map(
                status->{
                    Map<String, String> inValidRequestBody = Util // update status with invalid value.
                            .updateRequestBody(validRequestBody, Map.of("status", status), List.of());
                    return Arguments.of(inValidRequestBody, inValidStatusErrorMessage);
                }
        ).toList());

        // create argument with null description.
        arguments.add(Util
                .generateArgumentsFrom(Util // delete description from the request body.
                                .updateRequestBody(validRequestBody, Map.of(), List.of("description"))
                        ,  inValidDescriptionErrorMessage)
        );

        // create argument with null title.
        arguments.add(Util
                .generateArgumentsFrom(Util // delete title from the request body.
                                .updateRequestBody(validRequestBody, Map.of(), List.of("title"))
                        , inValidTitleErrorMessage)
        );

        // create argument with null status.
        arguments.add(Util
                .generateArgumentsFrom(Util // delete status from the request body.
                                .updateRequestBody(validRequestBody, Map.of(), List.of("status"))
                        , Map.of("status", "must not be null"))
        );

        return arguments.stream();
    }



}