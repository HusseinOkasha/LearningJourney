package com.example.EmployeeManager.service;

import com.example.EmployeeManager.dto.TaskDto;
import com.example.EmployeeManager.exception.AccountNotFoundException;
import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.model.Task;
import com.example.EmployeeManager.util.entityAndDtoMappers.TaskMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AccountTasksService {
    private final AccountService accountService;
    private final TaskService taskService;

    public AccountTasksService(AccountService accountService, TaskService taskService) {
        this.accountService = accountService;
        this.taskService = taskService;
    }

    public Set<Task> getMyTasks() {
        // extract the authentication object.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // extract the email of the authenticated ADMIN / EMPLOYEE from the authentication object.
        String email = authentication.getName();

        //fetch the account corspending to the extracted email.
        Account account = accountService
                .findAccountByEmail(email)
                .orElseThrow(
                        () -> new AccountNotFoundException("couldn't find account with email: " + email)
                );

        // get the tasks.
        return account.getTasks();

    }

    public Task addTaskToMyAccount(Task task){

        /*
         * It adds a task to the account of the authenticated (employee / admin).
         * */

        // extract the authentication object.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // extract the user email from the authentication object.
        String email = authentication.getName();

        // fetch the account from the database.
        Account account = accountService
                .findAccountByEmail(email)
                .orElseThrow(
                        () -> new AccountNotFoundException("couldn't find account with email: " + email)
                );

        // Save the task to the database.
        task = taskService.save(task);

        // add task to the account.
        account.getTasks().add(task);

        // save changes to account to the database.
        accountService.save(account);

        return task;
    }
}
