package com.example.EmployeeManager.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

// It represents a dto for updating the tasks' description.
public record UpdateDescriptionRequest(@NotNull @NotEmpty String description) {
}
