package com.example.EmployeeManager.controller;

import com.example.EmployeeManager.dto.CommentDto;
import com.example.EmployeeManager.model.*;
import com.example.EmployeeManager.service.AccountService;
import com.example.EmployeeManager.service.CommentService;
import com.example.EmployeeManager.service.TaskService;
import com.example.EmployeeManager.util.entityAndDtoMappers.CommentMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import org.hamcrest.core.Is;
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
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonParser;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.request;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;
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

    // sample comments
    private Comment adminComment;
    private Comment employeeComment;

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

    @BeforeEach
    void setUp() {

        RestAssured.baseURI = "http://localhost:" + port;
        RestAssured.defaultParser = Parser.JSON;
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
        adminComment = Comment
                .builder()
                .withBody("great work!")
                .withCreatedBy(admin)
                .build();

        employeeComment = Comment
                .builder()
                .withBody("nice")
                .withCreatedBy(employee)
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

    @Test
    void adminShouldGetCommentByUuid() {
        /*
         * It checks that ADMIN can get any comment by uuid.
         * Any comment means
         * comments on any task, which created by anyone.
         * It checks that the response status code is 200 (OK).
         * It checks that the returned comment has is the right comment.
         * checking that it's body is the same as the body of the intended comment.
         * Its uuid is the same as the intended one.
         * */

        // attempt authentication with account of role ADMIN
        String accessToken = util.attemptAuthenticationWith(admin);

        String fullUrl = String.format("%s/%s/comments/%s", apiUrl, task.getUuid(), adminComment.getUuid());
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(fullUrl)
                .then().body("body", equalTo(adminComment.getBody())).body("uuid",
                        equalTo(adminComment.getUuid().toString()))
                .statusCode(HttpStatus.OK.value()); // checks that the response status code is 200 (OK).

    }

    @Test
    void employeeShouldGetCommentByUuid() {
        /*
         * It checks that EMPLOYEE can get any comment by uuid.
         * Any comment means
         * comments on any task, which created by anyone.
         * It checks that the response status code is 200 (OK).
         * It checks that the returned comment has is the right comment.
         * checking that it's body is the same as the body of the intended comment.
         * Its uuid is the same as the intended one.
         * */

        // attempt authentication with account of role EMPLOYEE
        String accessToken = util.attemptAuthenticationWith(employee);

        String fullUrl = String.format("%s/%s/comments/%s", apiUrl, task.getUuid(), employeeComment.getUuid());
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(fullUrl)
                .then().body("body", equalTo(employeeComment.getBody())).body("uuid",
                        equalTo(employeeComment.getUuid().toString()))
                .statusCode(HttpStatus.OK.value()); // checks that the response status code is 200 (OK).
    }

    @Test
    void adminShouldNotGetCommentByUuidThatDoNotExist() {
        /*
         * It checks that ADMIN can't get any comment by uuid that doesn't exist on the database.
         * Any comment means:
         * comments on any task, which created by anyone.
         * It checks that the response status code is 404 (NOT_FOUND).

         * */

        // attempt authentication with account of role ADMIN
        String accessToken = util.attemptAuthenticationWith(admin);

        String fullUrl = String.format("%s/%s/comments/%s", apiUrl, task.getUuid(), UUID.randomUUID());
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(fullUrl)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void employeeShouldNotGetCommentByUuidThatDoNotExist() {
        /*
         * It checks that EMPLOYEE can't get any comment by uuid that doesn't exist on the database.
         * Any comment means:
         * comments on any task, which created by anyone.
         * It checks that the response status code is 404 (NOT_FOUND).

         * */

        // attempt authentication with account of role EMPLOYEE
        String accessToken = util.attemptAuthenticationWith(employee);

        String fullUrl = String.format("%s/%s/comments/%s", apiUrl, task.getUuid(), UUID.randomUUID());
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(fullUrl)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldNotGetCommentByUuidWithoutAccessToken() {
        /*
         * It checks that you can't get any comment by uuid without access token.
         * Any comment means:
         * comments on any task, which created by anyone.
         * It checks that the response status code is 401 (UN_AUTHORIZED).
         * */


        String fullUrl = String.format("%s/%s/comments/%s", apiUrl, task.getUuid(), adminComment.getUuid());
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(fullUrl)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void adminShouldGetAllCommentsOfTaskByTaskUuid() {
        /*
         * It tests that ADMIN can get all comments on a certain task.
         * It checks that the response status code is 200 (OK).
         * It checks that the size of the returned set of comments is as expected.
         * It checks the comments are the comments written on the task.
         * */

        // attempt authentication with account of role employee.
        String accessToken = util.attemptAuthenticationWith(employee);

        String fullUrl = String.format("%s/%s/comments", apiUrl, task.getUuid());

        List<CommentDto> expectedComments = Stream.of(employeeComment, adminComment)
                .map(CommentMapper::CommentToCommentDto)
                .toList();

        List<CommentDto> returnedComments = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(fullUrl)
                .then()
                .body("", hasSize(2))
                .statusCode(HttpStatus.OK.value())
                .extract().jsonPath().getList(".", CommentDto.class);
        returnedComments.forEach(commentDto -> assertThat(commentDto).isIn(returnedComments));


    }

    @Test
    void employeeShouldGetAllCommentsOfTaskByTaskUuid() {
        /*
         * It tests that EMPLOYEE can get all comments on a certain task.
         * It checks that the response status code is 200 (OK).
         * It checks that the size of the returned set of comments is as expected.
         * It checks the comments are the comments written on the task.
         * */

        // attempt authentication with account of role employee.
        String accessToken = util.attemptAuthenticationWith(employee);

        String fullUrl = String.format("%s/%s/comments", apiUrl, task.getUuid());

        List<CommentDto> expectedComments = Stream.of(employeeComment, adminComment)
                .map(CommentMapper::CommentToCommentDto)
                .toList();

        List<CommentDto> returnedComments = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(fullUrl)
                .then()
                .body("", hasSize(2))
                .statusCode(HttpStatus.OK.value())
                .extract().jsonPath().getList(".", CommentDto.class);
        returnedComments.forEach(commentDto -> assertThat(commentDto).isIn(returnedComments));


    }

    @Test
    void adminShouldNotGetAllCommentsOfTaskByUuidWithNonExistentTaskUuid() {
        /*
         * It tests that ADMIN can't get all comments on a certain task with nonexistent uuid .
         * It checks that the response status code is 404 (NOT_FOUND).
         * */

        // attempt authentication with account of role admin.
        String accessToken = util.attemptAuthenticationWith(admin);

        String fullUrl = String.format("%s/%s/comments", apiUrl, UUID.randomUUID());

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(fullUrl)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

    }

    @Test
    void employeeShouldNotGetAllCommentsOfTaskByUuidWithNonExistentTaskUuid() {
        /*
         * It tests that EMPLOYEE can't get all comments on a certain task with nonexistent uuid .
         * It checks that the response status code is 404 (NOT_FOUND).
         * */

        // attempt authentication with account of role employee.
        String accessToken = util.attemptAuthenticationWith(employee);

        String fullUrl = String.format("%s/%s/comments", apiUrl, UUID.randomUUID());

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(fullUrl)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

    }

    @Test
    void adminShouldUpdateHisCommentsByCommentUuid() {
        /*
         * This test verifies that an ADMIN can update any of their comments using the comment ID.
         * The following checks are performed:
         *   - The response status code is 201 (CREATED).
         *   - The comment body returned in the response matches the body sent in the request.
         *   - The comment UUID returned in the response matches the UUID sent in the request.
         */

        // attempt authentication with account of role admin.
        String accessToken = util.attemptAuthenticationWith(admin);

        String fullUrl = String.format("%s/%s/comments/%s", apiUrl, task.getUuid(), adminComment.getUuid());

        Map<String, String> requestBody = new HashMap<>();
        String updatedBody = "this a change in the comment body.";
        requestBody.put("body", updatedBody);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .put(fullUrl)
                .then()
                // checks that the comment body returned in the response matches the body sent in the request
                .body("body", equalTo(updatedBody))
                // checks that the comment uuid returned in the response matches the uuid sent in the request
                .body("uuid", equalTo(adminComment.getUuid().toString()))
                .statusCode(HttpStatus.CREATED.value());

    }

    @Test
    void employeeShouldUpdateHisCommentsByCommentUuid() {
        /*
         * This test verifies that an EMPLOYEE can update any of their comments using the comment ID.
         * The following checks are performed:
         *   - The response status code is 201 (CREATED).
         *   - The comment body returned in the response matches the body sent in the request.
         *   - The comment UUID returned in the response matches the UUID sent in the request.
         */

        // attempt authentication with account of role admin.
        String accessToken = util.attemptAuthenticationWith(employee);

        String fullUrl = String.format("%s/%s/comments/%s", apiUrl, task.getUuid(), employeeComment.getUuid());

        Map<String, String> requestBody = new HashMap<>();
        String updatedBody = "this a change in the comment body.";
        requestBody.put("body", updatedBody);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .put(fullUrl)
                .then()
                // checks that the comment body returned in the response matches the body sent in the request
                .body("body", equalTo(updatedBody))
                // checks that the comment uuid returned in the response matches the uuid sent in the request
                .body("uuid", equalTo(employeeComment.getUuid().toString()))
                .statusCode(HttpStatus.CREATED.value());

    }


    @Test
    void adminShouldNotUpdateHisCommentsByCommentUuidWithNullOrEmptyBody() {
        /*
         * This test verifies that an ADMIN can't update any of their comments using the comment ID with null / empty
         *  body.
         * The following checks are performed:
         *   - The response status code is 400 (BAD_REQUEST).
         */

        // attempt authentication with account of role admin.
        String accessToken = util.attemptAuthenticationWith(admin);

        String fullUrl = String.format("%s/%s/comments/%s", apiUrl, task.getUuid(), adminComment.getUuid());

        Map<String, String> requestBody = new HashMap<>();
        String updatedBody = "";
        requestBody.put("body", updatedBody);

        // checks that admin can't update comment with empty body.
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .put(fullUrl)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        // checks that the admin can't update comment with null body.
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .put(fullUrl)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void employeeShouldNotUpdateHisCommentsByCommentUuidWithNullOrEmptyBody() {
        /*
         * This test verifies that an EMPLOYEE can't update any of their comments using the comment ID with null /
         * empty body.
         * The following checks are performed:
         *   - The response status code is 400 (BAD_REQUEST).
         */

        // attempt authentication with account of role employee.
        String accessToken = util.attemptAuthenticationWith(employee);

        String fullUrl = String.format("%s/%s/comments/%s", apiUrl, task.getUuid(), employeeComment.getUuid());

        Map<String, String> requestBody = new HashMap<>();
        String updatedBody = "";
        requestBody.put("body", updatedBody);

        // checks that admin can't update comment with empty body.
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .put(fullUrl)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        // checks that the admin can't update comment with null body.
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .put(fullUrl)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void adminShouldNotUpdateCommentThatHeDidNotCreate() {
        /*
         * This test verifies that an ADMIN can't update comments he didn't create.
         * The following checks are performed:
         *   - The response status code is 404 (BAD_REQUEST).
         */

        // attempt authentication with account of role admin.
        String accessToken = util.attemptAuthenticationWith(admin);

        String fullUrl = String.format("%s/%s/comments/%s", apiUrl, task.getUuid(), employeeComment.getUuid());

        Map<String, String> requestBody = new HashMap<>();
        String updatedBody = "this is a comment update.";
        requestBody.put("body", updatedBody);


        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .put(fullUrl)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void employeeShouldNotUpdateCommentsHeDidNotCreate() {
        /*
         * This test verifies that an EMPLOYEE can't update comments he didn't create.
         * The following checks are performed:
         *   - The response status code is 400 (BAD_REQUEST).
         */

        // attempt authentication with account of role employee.
        String accessToken = util.attemptAuthenticationWith(employee);

        String fullUrl = String.format("%s/%s/comments/%s", apiUrl, task.getUuid(), adminComment.getUuid());

        Map<String, String> requestBody = new HashMap<>();
        String updatedBody = "this is a comment body update";
        requestBody.put("body", updatedBody);


        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .when()
                .put(fullUrl)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

    }


}