package com.example.project6.dto;

import com.example.project6.Enum.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTaskRequest(@NotBlank(message = "title shouldn't be empty nor blank") String title,
                                @NotBlank(message = "description shouldn't be empty nor blank") String description,
                                @NotNull TaskStatus status) {
}
