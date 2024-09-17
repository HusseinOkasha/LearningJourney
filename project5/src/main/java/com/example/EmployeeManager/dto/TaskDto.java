package com.example.EmployeeManager.dto;

import com.example.EmployeeManager.model.TaskStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TaskDto(@NotNull @NotEmpty String description,
                      @NotNull @NotEmpty String title,
                      @NotNull TaskStatus status,
                      UUID uuid) {
}
