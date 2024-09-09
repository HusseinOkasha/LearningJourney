package com.example.EmployeeManager.controller;


import com.example.EmployeeManager.dto.UpdateAccountDto;
import com.example.EmployeeManager.exception.NotFoundException;
import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.service.AccountService;
import com.example.EmployeeManager.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final AccountService accountService;
    private final AuthenticationService authenticationService;


    @Autowired
    public ProfileController(AccountService accountService, AuthenticationService authenticationService) {
        this.accountService = accountService;
        this.authenticationService = authenticationService;
    }

    @GetMapping
    public ResponseEntity<Account> getProfile(){
        // get the account of the currently authenticated (EMPLOYEE / ADMIN).
        Account account = authenticationService.getAuthenticatedAccount();
        return new ResponseEntity<>(account, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<Account> updateMyProfile(@RequestBody @Valid UpdateAccountDto updateAccountDto){

        // get the account of the currently authenticated (EMPLOYEE / ADMIN).
        Account account = authenticationService.getAuthenticatedAccount();

        account.setName(updateAccountDto.name());
        account.setJobTitle(updateAccountDto.jobTitle());
        account.setPhone(updateAccountDto.phone());

        account = accountService.save(account);
        return new ResponseEntity<>(account, HttpStatus.OK);
    }
}
