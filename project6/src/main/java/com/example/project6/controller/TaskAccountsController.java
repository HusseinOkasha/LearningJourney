package com.example.project6.controller;

import com.example.project6.Service.TaskAccountsService;
import com.example.project6.entity.TaskAccountLink;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/{taskUuid}/accounts/{accountUuid}")
    public ResponseEntity<List<TaskAccountLink>> shareTaskWithAccount(@PathVariable UUID taskUuid,
                                                                 @PathVariable UUID accountUuid){
        taskAccountsService.shareTaskWithAccount(taskUuid, accountUuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{taskUuid}/accounts/{accountUuid}")
    public ResponseEntity<List<TaskAccountLink>> unShareTaskWithAccount(@PathVariable UUID taskUuid,
                                                                 @PathVariable UUID accountUuid){
        taskAccountsService.unShareTaskWithAccount(taskUuid, accountUuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
