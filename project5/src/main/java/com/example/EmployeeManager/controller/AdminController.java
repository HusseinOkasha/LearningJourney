package com.example.EmployeeManager.controller;


import com.example.EmployeeManager.exception.AccountNotFoundException;
import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.model.Role;
import com.example.EmployeeManager.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/admin")
@RestController
public class AdminController {

    private final AccountService accountService;

    public AdminController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Account> addAdminAccount(@RequestBody Account account){
        /*
         * listens to requests using Http method "POST" on path "/api/admin".
         * Creates accounts of role Admin.
         * returns the created account.
         * */

        // makes sure that the account created is of type admin.
        account.setRole(Role.ADMIN);

        return new ResponseEntity<>(this.accountService.addAccount(account), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Account>> getAllAdmins(){
        /*
         * listens to requests using Http method "GET" on path "/api/admin".
         * returns all accounts of role ADMIN.
         * */
        return new ResponseEntity<>(this.accountService.findAllByRole(Role.ADMIN), HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<Account> getAdminByUuid(@PathVariable UUID uuid ){
        /*
         * listens to requests using Http method "GET" on path "/api/admin/{uuid}"
         * returns the account corresponding to the specified uuid.
         * */
        Account account = this.accountService.findByUuid(uuid).orElseThrow(
                ()-> new AccountNotFoundException("couldn't find the employee with")
        );
        return new ResponseEntity<>(account, HttpStatus.OK);
    }

}
