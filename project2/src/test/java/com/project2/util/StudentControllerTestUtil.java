package com.project2.util;

import com.project2.entity.Student;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class StudentControllerTestUtil {

    // used to send requests
    private RestTemplate restTemplate;

    private int port; //  the port number that the server is running on.

    public StudentControllerTestUtil(RestTemplate restTemplate, int port){
        this.restTemplate = restTemplate;
        this.port = port;
    }
    public ResponseEntity<Student> attemptGetStudentById(Long studentId){
        /*
         * It encapsulates the logic of sending a GET request to "api/student/{studentId}".
         * studentId: is the id  of the student we want to get.
         * */
        Request.Builder<String, Student> requestBuilder = new Request.Builder<>();

        Request<String , Student> request = requestBuilder
                .endPointUrl("/student/"+studentId)
                .portNumber(port)
                .restTemplate(restTemplate)
                .httpMethod(HttpMethod.GET)
                .responseDataType(new ParameterizedTypeReference<>(){})
                .build();

        return request.sendRequest();
    }
    public ResponseEntity<Student> attemptAdditionOfNewStudent(Student student){
        /*
         * It encapsulates the logic of sending a POST request to "api/student/{studentId}".
         * student: is the student to be added.
         * */
        Request.Builder<Student, Student> requestBuilder = new Request.Builder<>();

        Request<Student , Student> request = requestBuilder
                .endPointUrl("/student")
                .portNumber(port)
                .restTemplate(restTemplate)
                .httpMethod(HttpMethod.POST)
                .httpEntity(student)
                .responseDataType(new ParameterizedTypeReference<>(){})
                .build();

        return request.sendRequest();
    }
}
