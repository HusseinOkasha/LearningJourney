package com.example.EmployeeManager.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CommentDto(@NotNull(message = "comment body shouldn't be null.")
                         @NotEmpty(message = "comment body shouldn't be empty.") String body,
                         UUID uuid) {
}
