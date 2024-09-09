package com.example.EmployeeManager.service;

import com.example.EmployeeManager.exception.NotFoundException;
import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.model.Task;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

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
                        () -> new NotFoundException("couldn't find account with email: " + email)
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
                        () -> new NotFoundException("couldn't find account with email: " + email)
                );

        // Save the task to the database.
        task = taskService.save(task);

        // add task to the account.
        account.getTasks().add(task);

        // save changes to account to the database.
        accountService.save(account);

        return task;
    }

    public Task updateMyTaskTitleByUuid(UUID uuid, String title){
        /*
        * It takes the uuid of the task you want to update and the new title.
        * It returns the task after applying the update on it.
        * */

        // extract the authentication object.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // extract the email of the authenticated ADMIN / EMPLOYEE from the authentication object.
        String email = authentication.getName();

        // fetch the account of the authenticated ADMIN / EMPLOYEE from the database.
        Account account = accountService
                .findAccountByEmail(email)
                .orElseThrow(
                        ()-> new NotFoundException("Couldn't find account with email: " + email)
                );


        // fetch the task we want to update from the database.
        Task dbTask = taskService
                .findTaskByUuidAndAccount(uuid, account)
                .orElseThrow(()-> new NotFoundException("couldn't find account with email: " + email));

        // update the title.
        dbTask.setTitle(title);

        // save the update to the database.
        return taskService.save(dbTask);
    }
}
