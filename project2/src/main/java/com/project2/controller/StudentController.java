package com.project2.controller;

import com.project2.entity.Student;
import com.project2.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/student")
@RestController
public class StudentController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("{studentId}")
    Student getStudentById(@PathVariable Long studentId){
        return this.studentService.getStudentById(studentId);
    }
}
