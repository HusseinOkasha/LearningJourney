package com.example.project6.dto;

import com.example.project6.Enum.TaskStatus;

public record UpdateTaskStatusRequest(TaskStatus status) {
}
