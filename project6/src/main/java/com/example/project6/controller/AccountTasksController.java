package com.example.project6.controller;

import com.example.project6.Service.AccountTasksService;
import com.example.project6.Service.AuthenticationService;
import com.example.project6.entity.Account;
import com.example.project6.entity.AccountTaskLink;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/account")
public class AccountTasksController {

    private final AccountTasksService accountTasksService;

    public AccountTasksController(AccountTasksService accountTasksService) {
        this.accountTasksService = accountTasksService;
    }

    @GetMapping("/{accountUuid}/tasks")
    public ResponseEntity<List<AccountTaskLink>> getTasksByAccountUuid(@PathVariable UUID accountUuid){
        /*
         * Handle HTTP GET requests to api/account/{accountUuid}/tasks
         * In case of success it returns:
         *   - list of "AccountTaskLink" represents all task of the specified account.
         *   - HTTP STATUS CODE 200 OK.
         * */
        List<AccountTaskLink>tasks = accountTasksService.getAccountTasks(accountUuid);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }
    @GetMapping("/tasks")
    public ResponseEntity<List<AccountTaskLink>> getMyTasks(){
        /*
        * Handles HTTP GET requests to "api/account/tasks"
        * In case of success it returns:
        *   - list of "AccountTaskLink". represents links between the currently authenticated account and his tasks.
        *   - HTTP STATUS CODE 200 OK.
        * */

        List<AccountTaskLink>accountTaskLinks = accountTasksService.getMyTasks();
        return new ResponseEntity<>(accountTaskLinks, HttpStatus.OK);
    }
}
