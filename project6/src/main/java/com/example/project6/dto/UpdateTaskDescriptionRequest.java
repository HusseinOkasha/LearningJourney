package com.example.project6.dto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.lang.NonNull;

public record UpdateTaskDescriptionRequest(@NotBlank(message =  "description shouldn't be empty nor blank") String description) {
}
