package com.example.project6.dto;


import com.example.project6.Enum.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(String name,
                              @NotNull @Email String email,
                              @NotNull String password,
                              String jobTitle,
                              String phone,
                              @NotNull Role role) {
}
