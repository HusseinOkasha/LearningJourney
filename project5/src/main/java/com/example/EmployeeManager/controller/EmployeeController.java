package com.example.EmployeeManager.controller;

import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.model.Role;
import com.example.EmployeeManager.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/employee")
@RestController
public class EmployeeController {

    private final AccountService accountService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public EmployeeController(AccountService accountService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.accountService = accountService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Account> addAccount(@RequestBody Account account, Authentication auth){
        // listens to requests using Http method "POST" on path "/api/employee".

        // Encode the raw password.
        String rawPassword = account.getPassword();
        account.setPassword(bCryptPasswordEncoder.encode(rawPassword));
        return new ResponseEntity<>(this.accountService.addAccount(account), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<Account>> getAllAccounts(Authentication auth){
        // listens to requests using Http method "GET" on path "/api/employee" .
        return new ResponseEntity<>(this.accountService.findAllByRole(Role.USER), HttpStatus.OK);
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
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteAccountById(@PathVariable Long id){
         this.accountService.deleteAccount(id);
         return new ResponseEntity<>(HttpStatus.OK);
    }

}
