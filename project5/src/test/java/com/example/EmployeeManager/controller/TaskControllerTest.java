package com.example.EmployeeManager.controller;

import com.example.EmployeeManager.dto.TaskDto;
import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.model.Role;
import com.example.EmployeeManager.model.Task;
import com.example.EmployeeManager.model.TaskStatus;
import com.example.EmployeeManager.service.AccountService;
import com.example.EmployeeManager.service.TaskService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:tc:postgres:latest:///db",
                "spring.sql.init.mode=always"})
// to make sure that spring boot doesn't override the configuration and creates h2 database or any other in memory
// database.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class TaskControllerTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("db").withUsername("myuser");


    static final String apiUrl = "/api/task";


    @LocalServerPort
    private Integer port;

    // sample accounts.
    private Account admin;
    private Account employee;

    // sample task.
    private Task task;

    private final AccountService accountService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TaskService taskService;
    private final Util util;

    @Autowired
    TaskControllerTest(
            AccountService accountService,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            TaskService taskService, Util util
    ) {
        this.accountService = accountService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.taskService = taskService;
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

        // create sample task.
        task = Task
                .builder()
                .withTitle("fix an github issue")
                .withStatus(TaskStatus.TODO)
                .withDescription(" description")
                .build();

        // save sample accounts to the database.
        accountService.save(admin);
        accountService.save(employee);


        // save the sample task in the database.
        taskService.save(task);

        // add sample task to both employee and admin
        admin.getTasks().add(task);
        employee.getTasks().add(task);

        // save sample accounts to the database.
        accountService.save(admin);
        accountService.save(employee);

    }

    @AfterEach
    void tearDown() {
        accountService.deleteAll();

    }

    @Test
    public void adminShouldAddTask(){
        /*
        * tests that an account of role admin can create tasks for himself.
        * tests that the response status code is 201 (CREATED)
        *
        * */

        // attempt authentication with account of role ADMIN
        String accessToken = util.attemptAuthenticationWith(admin);

        // taskDto for the task to be created.
        TaskDto taskDto = new TaskDto("this is an important task","task 1", TaskStatus.TODO );



        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(taskDto)
                .when()
                .post(apiUrl)
                .then()
                .statusCode(HttpStatus.CREATED.value());
    }
    @Test
    public void employeeShouldAddTask(){
        /*
         * tests that an account of role employee can create tasks for himself.
         * tests that the response status code is 201 (CREATED)
         *
         * */

        // attempt authentication with account of role EMPLOYEE
        String accessToken = util.attemptAuthenticationWith(employee);

        // taskDto for the task to be created.
        TaskDto taskDto = new TaskDto("this is an important task","task 1", TaskStatus.TODO );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(taskDto)
                .when()
                .post(apiUrl)
                .then()
                .statusCode(HttpStatus.CREATED.value());
    }
    @Test
    public void adminShouldGetAllHisTasks(){
        /*
         * tests that an account of role admin get all his tasks.
         * tests that the response status code is 200 (OK).
         *
         *
         * */

        // attempt authentication with account of role ADMIN
        String accessToken = util.attemptAuthenticationWith(admin);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(apiUrl)
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void adminShouldUpdateTaskTitleByUuid(){
        /*
        * it tests that admin can update his tasks title by uuid.
        * it checks that the response status code is 200 (OK).
        * it checks that the returned task has the same title as one provided in the update request.
        * */


        // attempt authentication with account of role ADMIN
        String accessToken = util.attemptAuthenticationWith(admin);

        // value of new title.
        final String newTitle = "new title";

        // request body.
        Map<String, String> requestBody = Map.of("title", newTitle);

        // construct the url for update title endpoint.
        String fullUrl = String.format("%s/%s/title", apiUrl, task.getUuid());

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .patch(fullUrl)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("title", equalTo(newTitle));
    }

    @Test
    void employeeShouldUpdateTaskTitleByUuid(){
        /*
        * it tests that employee can update his tasks title by uuid.
        * it checks that the response status code is 200 (OK).
        * it checks that the returned task has the same title as one provided in the update request.
        * */


        // attempt authentication with account of role ADMIN
        String accessToken = util.attemptAuthenticationWith(employee);

        // value of new title.
        final String newTitle = "new title";

        // request body.
        Map<String, String> requestBody = Map.of("title", newTitle);

        // construct the url for update title endpoint.
        String fullUrl = String.format("%s/%s/title", apiUrl, task.getUuid());

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .patch(fullUrl)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("title", equalTo(newTitle));
    }

    @Test
    void adminShouldUpdateTaskDescriptionByUuid(){
        /*
        * It tests that account with role admin is able to update the description of one of his tasks by uuid
        * It checks that the response status code is 200 (OK).
        * It checks that the returned task in the response has the same description as the one provided in the request.
        * */

        // attempt authentication with account of role ADMIN
        String accessToken = util.attemptAuthenticationWith(admin);

        // value of new description.
        final String newDescription = "new description";

        // request body.
        Map<String, String> requestBody = Map.of("description", newDescription);

        // construct the url for update description endpoint.
        String fullUrl = String.format("%s/%s/description", apiUrl, task.getUuid());

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .patch(fullUrl)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("description", equalTo(newDescription));
    }

    @Test
    void employeeShouldUpdateTaskDescriptionByUuid(){
        /*
        * It tests that account with role EMPLOYEE is able to update the description of one of his tasks by uuid
        * It checks that the response status code is 200 (OK).
        * It checks that the returned task in the response has the same description as the one provided in the request.
        * */

        // attempt authentication with account of role ADMIN
        String accessToken = util.attemptAuthenticationWith(employee);

        // value of new description.
        final String newDescription = "new description";

        // request body.
        Map<String, String> requestBody = Map.of("description", newDescription);

        // construct the url for update description endpoint.
        String fullUrl = String.format("%s/%s/description", apiUrl, task.getUuid());

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .patch(fullUrl)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("description", equalTo(newDescription));
    }

    @Test
    void adminShouldUpdateTaskStatusByUuid(){
        /*
         * It tests that account with role admin is able to update the status of one of his tasks by uuid
         * It checks that the response status code is 200 (OK).
         * It checks that the returned task in the response has the same status as the one provided in the request.
         * */

        // attempt authentication with account of role ADMIN
        String accessToken = util.attemptAuthenticationWith(admin);

        // value of new status.
        // Why I have chosen IN_PROGRESS as it was created with TO_DO in the setUp method.
        final TaskStatus status = TaskStatus.IN_PROGRESS;

        // request body.
        Map<String, TaskStatus> requestBody = Map.of("status", status);

        // construct the url for update status endpoint.
        String fullUrl = String.format("%s/%s/status", apiUrl, task.getUuid());

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .patch(fullUrl)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("status", equalTo(status.toString()));
    }

    @Test
    void employeeShouldUpdateTaskStatusByUuid(){
        /*
         * It tests that account with role employee is able to update the status of one of his tasks by uuid
         * It checks that the response status code is 200 (OK).
         * It checks that the returned task in the response has the same status as the one provided in the request.
         * */

        // attempt authentication with account of role EMPLOYEE
        String accessToken = util.attemptAuthenticationWith(employee);

        // value of new status.
        // Why I have chosen IN_PROGRESS as it was created with TO_DO in the setUp method.
        final TaskStatus status = TaskStatus.IN_PROGRESS;

        // request body.
        Map<String, TaskStatus> requestBody = Map.of("status", status);

        // construct the url for update status endpoint.
        String fullUrl = String.format("%s/%s/status", apiUrl, task.getUuid());

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .patch(fullUrl)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("status", equalTo(status.toString()));
    }
    @Test
    void adminShouldNotUpdateTaskStatusByUuidUsingInvalidStatus(){
        /*
         * It tests that account with role admin can't update one of his tasks using invalid data
         * It checks that the response status code is 400 (BAD_REQUEST).
         * Invalid data means:
            * empty / null status.
            * status that is not specified in the TaskStatus enum.
         * */

        // attempt authentication with account of role EMPLOYEE
        String accessToken = util.attemptAuthenticationWith(admin);


        // construct the url for update status endpoint.
        String fullUrl = String.format("%s/%s/status", apiUrl, task.getUuid());

        // when the status is null.
        this.shouldNotUpdateTaskStatusByUuidUsingInvalidStatus(fullUrl, accessToken, new HashMap<>());

        // when the status is empty.
        this.shouldNotUpdateTaskStatusByUuidUsingInvalidStatus(fullUrl, accessToken, Map.of("status", ""));

        // when the status is not specified in the taskStatus enum.
        this.shouldNotUpdateTaskStatusByUuidUsingInvalidStatus(fullUrl, accessToken, Map.of("status", "konafa"));

    }
    @Test
    void employeeShouldNotUpdateTaskStatusByUuidUsingInvalidStatus(){
        /*
         * It tests that account with role employee can't update one of his tasks using invalid data
         * It checks that the response status code is 400 (BAD_REQUEST).
         * Invalid data means:
            * empty / null status.
            * status that is not specified in the TaskStatus enum.
         * */

        // attempt authentication with account of role EMPLOYEE
        String accessToken = util.attemptAuthenticationWith(employee);


        // construct the url for update status endpoint.
        String fullUrl = String.format("%s/%s/status", apiUrl, task.getUuid());

        // when the status is null.
        this.shouldNotUpdateTaskStatusByUuidUsingInvalidStatus(fullUrl, accessToken, new HashMap<>());

        // when the status is empty.
        this.shouldNotUpdateTaskStatusByUuidUsingInvalidStatus(fullUrl, accessToken, Map.of("status", ""));

        // when the status is not specified in the taskStatus enum.
        this.shouldNotUpdateTaskStatusByUuidUsingInvalidStatus(fullUrl, accessToken, Map.of("status", "konafa"));

    }

    @Test
    void adminShouldUpdateTaskByUuid(){
        /*
        * It tests that an account of role ADMIN can update his task by uuid.
        * It checks that the response status code is 200 (Ok).
        * It checks that the returned task is updated with the values passed in the request.
        * */

        // attempt authentication with account of role ADMIN
        String accessToken = util.attemptAuthenticationWith(admin);


        // request body.
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("title", "new title");
        requestBody.put("description", "new description");
        requestBody.put("status", TaskStatus.IN_PROGRESS.toString());


        // construct the url for update task endpoint.
        String fullUrl = String.format("%s/%s", apiUrl, task.getUuid());

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .put(fullUrl)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("status", equalTo(requestBody.get("status")))
                .body("title", equalTo(requestBody.get("title")))
                .body("description", equalTo(requestBody.get("description")));

    }
    @Test
    void employeeShouldUpdateTaskByUuid(){
        /*
        * It tests that an account of role EMPLOYEE can update his task by uuid.
        * It checks that the response status code is 200 (Ok).
        * It checks that the returned task is updated with the values passed in the request.
        * */

        // attempt authentication with account of role EMPLOYEE
        String accessToken = util.attemptAuthenticationWith(employee);


        // request body.
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("title", "new title");
        requestBody.put("description", "new description");
        requestBody.put("status", TaskStatus.IN_PROGRESS.toString());


        // construct the url for update task endpoint.
        String fullUrl = String.format("%s/%s", apiUrl, task.getUuid());

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .put(fullUrl)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("status", equalTo(requestBody.get("status")))
                .body("title", equalTo(requestBody.get("title")))
                .body("description", equalTo(requestBody.get("description")));

    }
    @Test
    void adminShouldNotUpdateTaskByUuidUsingInvalidData(){
        /*
        * It tests the ADMIN can't update task with invalid data.
        * In valid data means:
            * null / empty ( title, description or status)
            * status with value not specified in the TaskStatus enum.
        * It checks that the status response code is 400 (BAD_REQUEST).
        * */
        // construct the url for update task endpoint.
        String fullUrl = String.format("%s/%s", apiUrl, task.getUuid());

        // attempt authentication with account of role ADMIN
        String adminToken = util.attemptAuthenticationWith(admin);

        // request body.
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("title", "new title");
        requestBody.put("status", TaskStatus.IN_PROGRESS.toString());

        // in case of null description and account admin.
        shouldNotUpdateTaskByUuidUsingInvalidData(fullUrl, adminToken, requestBody);

        requestBody = new HashMap<>();
        requestBody.put("description", "new description");
        requestBody.put("status", TaskStatus.IN_PROGRESS.toString());

        // in case of null title and account admin.
        shouldNotUpdateTaskByUuidUsingInvalidData(fullUrl, adminToken, requestBody);

        requestBody = new HashMap<>();
        requestBody.put("title", "new title");
        requestBody.put("description", "new description");

        // in case of null status and account admin.
        shouldNotUpdateTaskByUuidUsingInvalidData(fullUrl, adminToken, requestBody);


        requestBody = new HashMap<>();
        requestBody.put("title", "");
        requestBody.put("description", "new description");
        requestBody.put("status", TaskStatus.IN_PROGRESS.toString());

        // in case of empty title and account admin.
        shouldNotUpdateTaskByUuidUsingInvalidData(fullUrl, adminToken, requestBody);


        requestBody = new HashMap<>();
        requestBody.put("title", "new title");
        requestBody.put("description", "");
        requestBody.put("status", TaskStatus.IN_PROGRESS.toString());

        // in case of empty description and account admin.
        shouldNotUpdateTaskByUuidUsingInvalidData(fullUrl, adminToken, requestBody);


        requestBody = new HashMap<>();
        requestBody.put("title", "new title");
        requestBody.put("description", "new description");
        requestBody.put("status", "");

        // in case of empty status and account admin.
        shouldNotUpdateTaskByUuidUsingInvalidData(fullUrl, adminToken, requestBody);


        requestBody = new HashMap<>();
        requestBody.put("title", "new title");
        requestBody.put("description", "new description");
        requestBody.put("status", "konafa");

        // in case of status with value isn't specified in the TaskStatus enum and account admin.
        shouldNotUpdateTaskByUuidUsingInvalidData(fullUrl, adminToken, requestBody);

    }

    void shouldNotUpdateTaskByUuidUsingInvalidData(String url, String accessToken, Map<String, String> requestBody){
        /*
         * It is a helper method.
         * It tests you can't update one of your tasks using invalid data
         * It checks that the response status code is 400 (BAD_REQUEST).
         */

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .put(url)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

    }
    void shouldNotUpdateTaskStatusByUuidUsingInvalidStatus(String url, String accessToken, Map<String, String> requestBody){
        /*
         * It tests you can't update one of your tasks using invalid data
         * It checks that the response status code is 400 (BAD_REQUEST).
         */

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .patch(url)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

    }
}