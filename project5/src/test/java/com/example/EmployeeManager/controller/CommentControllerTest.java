package com.example.EmployeeManager.controller;

import com.example.EmployeeManager.model.*;
import com.example.EmployeeManager.service.AccountService;
import com.example.EmployeeManager.service.CommentService;
import com.example.EmployeeManager.service.TaskService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
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
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.request;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:tc:postgres:latest:///db",
                "spring.sql.init.mode=always"})
// to make sure that spring boot doesn't override the configuration and creates h2 database or any other in memory
// database.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CommentControllerTest {
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
    private final CommentService commentService;

    @Autowired
    CommentControllerTest(AccountService accountService,
                          BCryptPasswordEncoder bCryptPasswordEncoder,
                          TaskService taskService,
                          Util util,
                          CommentService commentService) {
        this.accountService = accountService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.taskService = taskService;
        this.util = util;
        this.commentService = commentService;
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

    }

//    @BeforeAll
//    static void beforeAll(){
//        postgres.start();
//    }
//
//    @AfterAll
//    static void afterAll(){
//        postgres.stop();
//    }

    @BeforeEach
    void setUp() {
        // accountService.deleteAll();

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

        admin = accountService.save(admin);
        employee = accountService.save(employee);


        // create sample task.
        task = Task
                .builder()
                .withTitle("fix an github issue")
                .withStatus(TaskStatus.TODO)
                .withDescription("description")
                .build();

        taskService.save(task);

        // create sample comments.
        Comment adminComment = Comment
                .builder()
                .withBody("great work!")

                .build();

        Comment employeeComment = Comment
                .builder()
                .withBody("nice")
                .build();

        commentService.save(adminComment);
        commentService.save(employeeComment);


        // wire sample accounts with the task.
        admin.getTasks().add(task);
        employee.getTasks().add(task);

        accountService.save(admin);
        accountService.save(employee);

        // add comments to the task.
        task.getComments().add(adminComment);
        task.getComments().add(employeeComment);

        taskService.save(task);


    }

    @AfterEach
    void tearDown() {


        accountService.findAllAccounts().forEach(
                acc -> {
                    acc.setTasks(null);
                    accountService.save(acc);
                }
        );
        commentService.deleteAll();

        taskService.deleteAll();
        accountService.deleteAll();
    }

    @Test
    void adminShouldAddCommentToHisTasks() {
        /*
         * It tests that admin can add comments to his task.
         * It checks that the response status code is 201 created.
         * It checks the existence of his comment by calling method findByUuidAndAccountAndTask
         * checks that the returned comment has body equal to the body sent with the request.
         *
         * */

        // attempt authentication with account of role ADMIN
        String accessToken = util.attemptAuthenticationWith(admin);

        // add the comment to the request body.
        Map<String, String> requestBody = new HashMap<>();

        requestBody.put("body", "great work.");

        String fullUrl = String.format("%s/%s/comments", apiUrl, task.getUuid());
        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .post(fullUrl)
                .then()
                // checks that the returned comment has the same body as the one sent with the request.
                .body("body", equalTo(requestBody.get("body")))
                // checks that the response status code is 201 CREATED.
                .statusCode(HttpStatus.CREATED.value())
                .extract().response();

        UUID commentUuid = UUID.fromString(response.jsonPath().getString("uuid"));

        // checks that the returned comment has the same body as the one sent with the request.
        assertThat(requestBody.get("body"))
                .isEqualTo(
                        commentService
                                .findByUuid(commentUuid)
                                .getBody()
                );


    }

    @Test
    void employeeShouldAddCommentToHisTasks() {
        /*
         * It tests that employee can add comments to his task.
         * It checks that the response status code is 201 created.
         * It checks the existence of his comment by calling method findByUuidAndAccountAndTask
         * checks that the returned comment has body equal to the body sent with the request.
         *
         * */

        // attempt authentication with account of role ADMIN
        String accessToken = util.attemptAuthenticationWith(employee);

        // add the comment to the request body.
        Map<String, String> requestBody = new HashMap<>();

        requestBody.put("body", "great work.");

        String fullUrl = String.format("%s/%s/comments", apiUrl, task.getUuid());
        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .post(fullUrl)
                .then()
                // checks that the returned comment has the same body as the one sent with the request.
                .body("body", equalTo(requestBody.get("body")))
                // checks that the response status code is 201 CREATED.
                .statusCode(HttpStatus.CREATED.value())
                .extract().response();

        UUID commentUuid = UUID.fromString(response.jsonPath().getString("uuid"));

        // checks that the returned comment has the same body as the one sent with the request.
        assertThat(requestBody.get("body"))
                .isEqualTo(
                        commentService
                                .findByUuid(commentUuid)
                                .getBody()
                );
    }

    @Test
    void adminShouldNotAddCommentToHisTasksWithEmptyBody() {
        /*
         * It tests that admin can't add comments to his task with empty body.
         * It checks that the response status code is 400 BAD_REQUEST.
         *
         * */

        // attempt authentication with account of role ADMIN
        String accessToken = util.attemptAuthenticationWith(admin);

        // add the comment to the request body.
        Map<String, String> requestBody = new HashMap<>();

        requestBody.put("body", "");

        String fullUrl = String.format("%s/%s/comments", apiUrl, task.getUuid());
        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .post(fullUrl)
                .then()
                // checks that the response status code is 400 BAD_REQUEST.
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().response();

    }

    @Test
    void employeeShouldNotAddCommentToHisTasksWithEmptyBody() {
        /*
         * It tests that employee can't add comments to his task with empty body.
         * It checks that the response status code is 400 BAD_REQUEST.
         *
         * */

        // attempt authentication with account of role ADMIN
        String accessToken = util.attemptAuthenticationWith(employee);

        // add the comment to the request body.
        Map<String, String> requestBody = new HashMap<>();

        requestBody.put("body", "");

        String fullUrl = String.format("%s/%s/comments", apiUrl, task.getUuid());
        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .post(fullUrl)
                .then()
                // checks that the response status code is 400 BAD_REQUEST.
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().response();

    }

    @Test
    void employeeShouldNotAddCommentToHisTasksNullBody() {
        /*
         * It tests that employee can't add comments to his task with null body.
         * It checks that the response status code is 400 BAD_REQUEST.
         *
         * */

        // attempt authentication with account of role ADMIN
        String accessToken = util.attemptAuthenticationWith(employee);

        // add the comment to the request body.
        Map<String, String> requestBody = new HashMap<>();

        String fullUrl = String.format("%s/%s/comments", apiUrl, task.getUuid());
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .post(fullUrl)
                .then()
                // checks that the response status code is 400 BAD_REQUEST.
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().response();

    }

    @Test
    void adminShouldNotAddCommentToHisTasksWithNullBody() {
        /*
         * It tests that admin can't add comments to his task with empty body.
         * It checks that the response status code is 400 BAD_REQUEST.
         *
         * */

        // attempt authentication with account of role ADMIN
        String accessToken = util.attemptAuthenticationWith(admin);

        String fullUrl = String.format("%s/%s/comments", apiUrl, task.getUuid());
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .post(fullUrl)
                .then()
                // checks that the response status code is 400 BAD_REQUEST.
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().response();
    }

    @Test
    void shouldNotAddCommentToTasksWithoutAccessToken() {
        /*
         * It tests that admin can't add comments to his task with empty body.
         * It checks that the response status code is 401 UNAUTHORIZED.
         *
         * */
        String fullUrl = String.format("%s/%s/comments", apiUrl, task.getUuid());
        given()
                .contentType(ContentType.JSON)
                .when()
                .post(fullUrl)
                .then()
                // checks that the response status code is 400 BAD_REQUEST.
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .extract().response();
    }


}