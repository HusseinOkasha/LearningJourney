package com.example.project6.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskTitleRequest(@NotBlank(message = "title shouldn't be empty nor blank") String title) {
}
