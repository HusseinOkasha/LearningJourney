package com.example.EmployeeManager.util.entityAndDtoMappers;

import com.example.EmployeeManager.dto.TaskDto;
import com.example.EmployeeManager.model.Task;

public class TaskMapper {

    public static TaskDto taskEntityToTaskDto(Task task){
        // converts task entity to task dto.
        return new TaskDto(task.getDescription(), task.getTitle(), task.getStatus());
    }

    public static Task taskDtoToTaskEntity(TaskDto taskDto){

        // converts task dto to task entity.
        return Task.builder()
                .withDescription(taskDto.description())
                .withTitle(taskDto.title())
                .withStatus(taskDto.status())
                .build();
    }


}
