package com.example.EmployeeManager.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateTitleRequest(@NotNull @NotEmpty String title) {
}
