package com.example.project6.dto;

import com.example.project6.Enum.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateTaskRequest(@NotNull @NotBlank String title,
                                @NotNull @NotBlank String description,
                                @NotNull TaskStatus status) {
}
