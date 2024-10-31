package com.example.project6.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TaskAccountDto(@NotNull  UUID accountUuid,
                             @NotBlank  String accountName,
                             @NotNull  UUID taskUuid,
                             @NotBlank  String taskTitle) {
}
