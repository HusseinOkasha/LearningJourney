package com.example.project6.controller;

import com.example.project6.Service.TaskAccountsService;
import com.example.project6.entity.TaskAccountLink;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/task")
public class TaskAccountsController {
    private final TaskAccountsService taskAccountsService;

    public TaskAccountsController(TaskAccountsService taskAccountsService) {
        this.taskAccountsService = taskAccountsService;
    }

    @GetMapping("/{taskUuid}/accounts")
    public ResponseEntity<List<TaskAccountLink>> getTaskAccounts(@PathVariable UUID taskUuid){
        return new ResponseEntity<>(taskAccountsService.getTaskAccounts(taskUuid), HttpStatus.OK);
    }
}
