package com.example.EmployeeManager.controller;

import com.example.EmployeeManager.model.Employee;
import com.example.EmployeeManager.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/employee")
@RestController
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<Employee> addEmployee(@RequestBody Employee employee){
        return new ResponseEntity<>(this.employeeService.addEmployee(employee), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees(){
        return new ResponseEntity<>(this.employeeService.findAllEmployees(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id ){
        return new ResponseEntity<>(this.employeeService.findEmployeeById(id), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<Employee> updateEmployeeById(@RequestBody Employee employee){
        return new ResponseEntity<>(this.employeeService.updateEmployee(employee), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployeeById(@PathVariable Long id){
         this.employeeService.deleteEmployee(id);
         return new ResponseEntity<>(HttpStatus.OK);
    }

}
