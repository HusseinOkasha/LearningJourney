package com.example.EmployeeManager.controller;

import com.example.EmployeeManager.dao.AccountRepository;
import com.example.EmployeeManager.dto.TaskDto;
import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.model.Role;
import com.example.EmployeeManager.model.Task;
import com.example.EmployeeManager.model.TaskStatus;
import com.example.EmployeeManager.service.AccountService;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
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


}