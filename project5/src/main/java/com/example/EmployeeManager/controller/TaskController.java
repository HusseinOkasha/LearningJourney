package com.example.EmployeeManager.controller;


import com.example.EmployeeManager.dto.TaskDto;
import com.example.EmployeeManager.exception.AccountNotFoundException;
import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.model.Task;
import com.example.EmployeeManager.service.AccountService;
import com.example.EmployeeManager.service.AccountTasksService;
import com.example.EmployeeManager.service.TaskService;
import com.example.EmployeeManager.util.entityAndDtoMappers.TaskMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    private final AccountTasksService accountTasksService;

    @Autowired
    public TaskController(AccountTasksService accountTasksService) {
        this.accountTasksService= accountTasksService;
    }

    @PostMapping
    public ResponseEntity<TaskDto> createTask(@RequestBody @Valid TaskDto taskDto){

        // create task entity from the task dto.
        Task task = TaskMapper.taskDtoToTaskEntity(taskDto);

        // add the task to the authenticated users' account.
        Task createdTask = accountTasksService.addTaskToMyAccount(task);

        // convert the task entity to taskDto and return it in the response body.
        return new ResponseEntity<>(TaskMapper.taskEntityToTaskDto(createdTask), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Set<TaskDto>> getMyTasks(){
        return new ResponseEntity<>(
                accountTasksService
                        .getMyTasks()
                        .stream()
                        .map(TaskMapper::taskEntityToTaskDto).collect(Collectors.toSet()),
                HttpStatus.OK);
    }

}
