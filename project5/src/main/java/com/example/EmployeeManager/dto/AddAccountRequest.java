package com.example.EmployeeManager.dto;

import com.example.EmployeeManager.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record AddAccountRequest(String name,
                                @NotNull @Email String email,
                                @NotNull String password,
                                String jobTitle,
                                String phone,
                                @NotNull Role role) {
}
