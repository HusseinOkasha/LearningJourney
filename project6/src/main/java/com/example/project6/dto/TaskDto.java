package com.example.project6.dto;

import com.example.project6.Enum.TaskStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;


public record TaskDto (@NotNull @NotBlank(message = "title shouldn't be empty nor blank") String title,
                       @NotNull @NotBlank(message = "description shouldn't be empty nor blank") String description,
                       @Valid TaskStatus taskStatus,
                       @NotNull  UUID taskUuid){
}
