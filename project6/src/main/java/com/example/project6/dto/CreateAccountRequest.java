package com.example.project6.dto;

import com.example.project6.Enum.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;


public record CreateAccountRequest (@Email @NotNull String email,
                                    @NotNull @NotEmpty  String name,
                                    @NotNull @NotEmpty  String password,
                                    @NotNull Role role) {
}
