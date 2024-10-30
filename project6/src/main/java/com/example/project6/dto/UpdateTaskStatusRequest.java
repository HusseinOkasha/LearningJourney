package com.example.project6.dto;

import com.example.project6.Enum.TaskStatus;
import jakarta.validation.constraints.NotNull;


public record UpdateTaskStatusRequest(@NotNull TaskStatus status) {
}
