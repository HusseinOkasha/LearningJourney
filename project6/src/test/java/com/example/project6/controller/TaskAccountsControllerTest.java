package com.example.project6.controller;

import com.example.project6.Service.AccountService;
import com.example.project6.Service.AccountTasksService;
import com.example.project6.Service.TaskAccountsService;
import com.example.project6.Service.TaskService;
import com.example.project6.Util;
import com.example.project6.entity.Account;
import com.example.project6.entity.AccountTaskLink;
import com.example.project6.entity.Task;
import com.example.project6.entity.TaskAccountLink;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.List;

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
    }

    @Test
    void Test (){

    }
}