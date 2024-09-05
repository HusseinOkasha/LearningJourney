package com.example.EmployeeManager.controller;


import com.example.EmployeeManager.dto.TaskDto;
import com.example.EmployeeManager.exception.AccountNotFoundException;
import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.model.Task;
import com.example.EmployeeManager.service.AccountService;
import com.example.EmployeeManager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    private final TaskService taskService;
    private final AccountService accountService;

    @Autowired
    public TaskController(TaskService taskService, AccountService accountService) {
        this.taskService = taskService;
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody @Valid TaskDto taskDto){
        taskService.addTaskToAccount(taskDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
