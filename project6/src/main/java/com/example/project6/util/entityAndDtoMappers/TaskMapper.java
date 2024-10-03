package com.example.project6.util.entityAndDtoMappers;

import com.example.project6.dto.CreateTaskRequest;
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
}