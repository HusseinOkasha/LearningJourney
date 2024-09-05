package com.example.EmployeeManager.controller;

import com.example.EmployeeManager.dto.AddAccountRequest;
import com.example.EmployeeManager.exception.AccountNotFoundException;
import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.model.Role;
import com.example.EmployeeManager.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/admin/employees")
@RestController
public class EmployeeController {

    private final AccountService accountService;

    @Autowired
    public EmployeeController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Account> addEmployee(@Valid  @RequestBody AddAccountRequest request){
        /*
         * listens to requests using Http method "POST" on path "/api/admin/employees".
         * Creates accounts of role employee.
         * returns the created account.
         * */

        // makes sure that the account created is of type employee.
        // create account from request.
        Account account = Account.builder()
                .withEmail(request.email())
                .withPassword(request.password())
                .withJobTitle(request.jobTitle())
                .withName(request.name())
                .withPhone(request.phone())
                .withRole(Role.EMPLOYEE)
                .build();
        return new ResponseEntity<>(this.accountService.addAccount(account), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Account>> getAllEmployees(){
        /*
        * listens to requests using Http method "GET" on path "/api/admin/employee".
        * returns all accounts of role EMPLOYEE.
        * */
        return new ResponseEntity<>(this.accountService.findAllByRole(Role.EMPLOYEE), HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<Account> getEmployeeByUuid(@PathVariable UUID uuid ){
        /*
        * listens to requests using Http method "GET" on path "/api/admin/employee/{uuid}"
        * returns the account corresponding to the specified uuid.
        * */
        Account account = this.accountService.findByUuid(uuid).orElseThrow(
                ()-> new AccountNotFoundException("couldn't find the employee with")
        );
        return new ResponseEntity<>(account, HttpStatus.OK);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteEmployeeByUuid(@PathVariable UUID uuid){
         this.accountService.deleteAccountByUuidAndRole(uuid, Role.EMPLOYEE);
         return new ResponseEntity<>(HttpStatus.OK);
    }

}
