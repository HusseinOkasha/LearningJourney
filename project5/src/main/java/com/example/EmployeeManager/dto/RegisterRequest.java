package com.example.EmployeeManager.dto;

import com.example.EmployeeManager.model.Role;

public record RegisterRequest(String name,
                              String email,
                              String password,
                              String jobTitle,
                              String phone,
                              Role role) {
}
