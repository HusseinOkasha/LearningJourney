package com.example.project6.controller;

import com.example.project6.Service.AccountTasksService;
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
}
