package com.example.project6.controller;


import com.example.project6.Service.TaskService;
import com.example.project6.dto.CreateTaskRequest;
import com.example.project6.dto.TaskDto;
import com.example.project6.entity.Task;
import com.example.project6.util.entityAndDtoMappers.TaskMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    final private TaskService taskService;


    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/{accountUuid}")
    public void addTask(@RequestBody CreateTaskRequest request, @PathVariable UUID accountUuid){
        Task task = TaskMapper.createTaskRequestToTaskEntity(request);
        taskService.addTaskToAccount(accountUuid, task);
    }

    @GetMapping("/{taskUuid}")
    public ResponseEntity<TaskDto> getTaskByUuid(@PathVariable UUID taskUuid){

        Task task = taskService.getTaskByUuid(taskUuid);

        // convert task to taskDto
        TaskDto taskDto = TaskMapper.TaskEntityToTaskDto(taskService.getTaskByUuid(taskUuid));

        return new ResponseEntity<>(taskDto, HttpStatus.OK);
    }
}
