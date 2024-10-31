package com.example.project6.util.entityAndDtoMappers;

import com.example.project6.dto.TaskAccountDto;
import com.example.project6.entity.TaskAccountLink;

public class TaskAccountMapper {

    public static TaskAccountDto TaskAccountToTaskAccountDto(TaskAccountLink taskAccountLink){
        return new TaskAccountDto(
                taskAccountLink.getAccountUuid(),
                taskAccountLink.getAccountName(),
                taskAccountLink.getTaskUuid(),
                taskAccountLink.getTaskTitle()
        );
    }
}
