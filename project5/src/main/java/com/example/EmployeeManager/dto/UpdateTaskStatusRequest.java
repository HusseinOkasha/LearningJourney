package com.example.EmployeeManager.dto;

import com.example.EmployeeManager.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(@NotNull TaskStatus status) {
}
