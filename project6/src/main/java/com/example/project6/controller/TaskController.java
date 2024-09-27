package com.example.project6.controller;


import com.example.project6.Service.TaskService;
import com.example.project6.dto.CreateTaskRequest;
import com.example.project6.entity.DBItem;

import com.example.project6.util.entityAndDtoMappers.DBItemMapper;
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
        DBItem item = DBItemMapper.createTaskRequestToDBItem(request);
        taskService.addTaskToAccount(accountUuid, item);
    }
}
