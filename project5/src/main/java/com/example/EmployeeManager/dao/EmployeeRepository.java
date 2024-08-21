package com.example.EmployeeManager.dao;

import com.example.EmployeeManager.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
