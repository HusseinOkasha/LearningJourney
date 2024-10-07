package com.example.project6.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskTitleRequest(@NotNull @NotBlank String title) {
}
