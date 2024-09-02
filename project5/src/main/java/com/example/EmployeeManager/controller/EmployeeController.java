package com.example.EmployeeManager.controller;

import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/employee")
@RestController
public class EmployeeController {

    private final AccountService accountService;

    @Autowired
    public EmployeeController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Account> addAccount(@RequestBody Account account){
        // listens to requests using Http method "POST" on path "/api/employee" .
        return new ResponseEntity<>(this.accountService.addAccount(account), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts(){
        // listens to requests using Http method "GET" on path "/api/employee" .
        return new ResponseEntity<>(this.accountService.findAllAccounts(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable Long id ){
        return new ResponseEntity<>(this.accountService.findAccountById(id), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<Account> updateAccountById(@RequestBody Account account){
        return new ResponseEntity<>(this.accountService.updateAccount(account), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccountById(@PathVariable Long id){
         this.accountService.deleteAccount(id);
         return new ResponseEntity<>(HttpStatus.OK);
    }

}
