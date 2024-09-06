package com.example.EmployeeManager.service;

import com.example.EmployeeManager.controller.Auth;
import com.example.EmployeeManager.dao.AccountRepository;
import com.example.EmployeeManager.dao.TaskRepository;
import com.example.EmployeeManager.dto.AuthenticationRequest;
import com.example.EmployeeManager.dto.TaskDto;
import com.example.EmployeeManager.exception.AccountNotFoundException;
import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class TaskService {

    @Autowired
    private final AccountService accountService;
    @Autowired
    private final TaskRepository taskRepository;

    public TaskService(AccountService accountService, TaskRepository taskRepository) {
        this.accountService = accountService;
        this.taskRepository = taskRepository;
    }

    public Optional<Task> findTaskByUuid(UUID uuid) {
        return this.taskRepository.findByUuid(uuid);
    }

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public Task save(Task task) {
        return this.taskRepository.save(task);
    }

    public void deleteTaskByUuid(UUID uuid) {
        taskRepository.deleteByUuid(uuid);
    }

}
