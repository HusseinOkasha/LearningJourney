package com.project2.controller;

import com.project2.dao.StudentRepository;
import com.project2.entity.Student;
import com.project2.service.StudentService;
import com.project2.util.StudentControllerTestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.datasource.url=jdbc:tc:postgres:latest:///db", "spring.sql.init.mode=always"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StudentControllerTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("db").withUsername("myuser");

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @LocalServerPort
    private int port; // holds the random port number.

    private Student student; //sample student

    private StudentControllerTestUtil testUtil;
    @BeforeEach
    void setUp() {
        student = new Student("Hussein", "Okasha", "e1@gmail.com", LocalDate.of(2010, 6, 5));
        studentRepository.save(student);

        testUtil = new StudentControllerTestUtil(restTemplate, port);
    }

    @AfterEach
    void tearDown() {
        studentService.deleteAllStudents();
    }

    @Test
    void shouldGetStudentById() {
        // tests that we can get a student by id.
            ResponseEntity<Student> response = testUtil.attemptGetStudentById(student.getId());
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
    @Test
    void shouldNotGetStudentById(){
        // tests that we can't get a student, if there is no student having the provided id in the database.

        // Here I have used + 1 to represent a nonexistent id
        try {
            ResponseEntity<Student> response = testUtil.attemptGetStudentById(student.getId()+1);
        }
        catch (HttpServerErrorException e){
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Test
    void shouldAddNewStudent() {
        // tests that we can add new student with an email that doesn't exist in the student table
        Student newStudent =
                new Student("Ahmed", "Mohammed", "ahmed@gmail.com", LocalDate.of(2010,10,14));

        ResponseEntity<Student> response = testUtil.attemptAdditionOfNewStudent(newStudent);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldNotAddNewStudent() {
        // tests that we can't add new student with an email that  exists in the student table
        ResponseEntity<Student> response = testUtil.attemptAdditionOfNewStudent(student);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}