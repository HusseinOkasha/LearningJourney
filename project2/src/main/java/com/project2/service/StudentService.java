package com.project2.service;

import com.project2.dao.StudentRepository;
import com.project2.entity.Student;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student getStudentById(Long studentId) throws IllegalStateException {
        Optional<Student> studentOptional = studentRepository.findById(studentId);
        return studentOptional.orElseThrow(() -> new IllegalStateException("there is no student with this  id"));
    }

    @Transactional
    public Student addNewStudent(Student student) {
        Boolean exist = studentRepository.existsByEmail(student.getEmail());
        if(exist){
            throw new IllegalStateException("this email already exists");
        }
        return studentRepository.save(student);
    }
    public void deleteAllStudents(){
        studentRepository.deleteAll();
    }
}
