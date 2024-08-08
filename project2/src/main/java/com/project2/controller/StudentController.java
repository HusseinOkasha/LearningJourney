package com.project2.controller;

import com.project2.entity.Student;
import com.project2.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("")
    Student addNewStudent(@RequestBody Student student){
        return this.studentService.addNewStudent(student);
    }
}
