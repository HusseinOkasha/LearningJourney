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
    private final AuthenticationService authenticationService;

    public AccountTasksService(AccountService accountService, TaskService taskService, AuthenticationService authenticationService) {
        this.accountService = accountService;
        this.taskService = taskService;
        this.authenticationService = authenticationService;
    }

    public Set<Task> getMyTasks() {
        // fetch the account of the authenticated ADMIN / EMPLOYEE.
        Account account = authenticationService.getAuthenticatedAccount();

        // get the tasks.
        return account.getTasks();

    }

    public Task addTaskToMyAccount(Task task){

        /*
         * It adds a task to the account of the authenticated (employee / admin).
         * */

        // fetch the account of the authenticated ADMIN / EMPLOYEE.
        Account account = authenticationService.getAuthenticatedAccount();

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

        // fetch the account of the authenticated ADMIN / EMPLOYEE.
        Account account = authenticationService.getAuthenticatedAccount();

        // fetch the task we want to update from the database.
        Task dbTask = taskService
                .findTaskByUuidAndAccount(uuid, account)
                .orElseThrow(()-> new NotFoundException("couldn't find task with uuid: " + uuid));

        // update the title.
        dbTask.setTitle(title);

        // save the update to the database.
        return taskService.save(dbTask);
    }
}
