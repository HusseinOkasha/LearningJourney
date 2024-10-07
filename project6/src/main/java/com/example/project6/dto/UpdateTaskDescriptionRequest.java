package com.example.project6.dto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.lang.NonNull;

public record UpdateTaskDescriptionRequest(@NonNull @NotBlank String description) {
}
