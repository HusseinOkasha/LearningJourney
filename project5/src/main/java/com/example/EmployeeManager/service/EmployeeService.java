package com.example.EmployeeManager.service;

import com.example.EmployeeManager.dao.EmployeeRepository;
import com.example.EmployeeManager.exception.EmployeeNotFoundException;
import com.example.EmployeeManager.model.Employee;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Employee addEmployee(Employee employee) {
        employee.setEmployeeCode(UUID.randomUUID().toString());
        return employeeRepository.save(employee);
    }

    public List<Employee> findAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee updateEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    public Employee findEmployeeById(Long id) {
        return employeeRepository
                .findById(id)
                .orElseThrow(
                        () -> new EmployeeNotFoundException(
                                String.format("%s%s%s", "employee with id ", id, " not found")
                        )
                );
    }

    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }

}
