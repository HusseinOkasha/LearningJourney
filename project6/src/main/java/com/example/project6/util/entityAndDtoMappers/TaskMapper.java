package com.example.project6.util.entityAndDtoMappers;

import com.example.project6.dto.CreateTaskRequest;
import com.example.project6.dto.TaskDto;
import com.example.project6.dto.UpdateTaskRequest;
import com.example.project6.entity.Task;

public class TaskMapper {
    static public Task createTaskRequestToTaskEntity(CreateTaskRequest request){
        return Task
                .builder()
                .withTitle(request.title())
                .withStatus(request.status())
                .withDescription(request.description())
                .build();
    }

    static public TaskDto TaskEntityToTaskDto(Task task){
        return new TaskDto(task.getTitle(), task.getDescription(), task.getStatus(), task.getTaskUuid());
    }

    public static Task updateTaskRequestToTaskEntity(UpdateTaskRequest request) {
        return Task.builder()
                .withTitle(request.title())
                .withDescription(request.description())
                .withStatus(request.status())
                .build();
    }
}
