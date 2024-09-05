package com.example.EmployeeManager.dto;

import com.example.EmployeeManager.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record UpdateAccountDto(String name,
                               String jobTitle,
                               String phone) {
}
