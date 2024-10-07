package com.example.project6.dto;

import com.example.project6.Enum.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskRequest(@NotNull @NotBlank String title,
                                @NotNull @NotBlank String description,
                                @NotNull TaskStatus status) {
}
