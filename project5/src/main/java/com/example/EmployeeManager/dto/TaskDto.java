package com.example.EmployeeManager.dto;

import com.example.EmployeeManager.model.TaskStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record TaskDto(@NotNull @NotEmpty String description,
                      @NotNull @NotEmpty String title,
                      @NotNull TaskStatus status) {
}