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

@Service
public class TaskService {

    @Autowired
    private final AccountRepository accountRepository;
    @Autowired
    private final TaskRepository taskRepository;

    public TaskService(AccountRepository accountRepository, TaskRepository taskRepository) {
        this.accountRepository = accountRepository;
        this.taskRepository = taskRepository;
    }

    public void addTaskToAccount(TaskDto taskDto){
        // extract the authentication object.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // build task object.
        Task task = Task.builder()
                .withDescription(taskDto.description())
                .withStatus(taskDto.status())
                .withTitle(taskDto.title())
                .build();

        // Save the task to the database.
        taskRepository.save(task);

        // Extract the email from the authentication object.
        String email = authentication.getName();

        // fetch the account from the database.
        Account account = accountRepository
                .findByEmail(email)
                .orElseThrow(()-> new AccountNotFoundException("couldn't find account with email: " + email));

        // add task to the account.
        account.getTasks().add(task);

        // save changes to account to the database.
        accountRepository.save(account);
    }
}
