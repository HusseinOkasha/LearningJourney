package com.example.EmployeeManager.controller;


import com.example.EmployeeManager.dto.UpdateAccountDto;
import com.example.EmployeeManager.exception.AccountNotFoundException;
import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.service.AccountService;
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

    @Autowired
    public ProfileController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<Account> getProfile(Authentication authentication){
        String email = authentication.getName();
        return new ResponseEntity<>(
                accountService
                        .findAccountByEmail(email)
                        .orElseThrow(()->new AccountNotFoundException("couldn't find account with email " + email)
                        ),
                HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<Account> updateMyProfile(@RequestBody @Valid UpdateAccountDto updateAccountDto, Authentication authentication){
        String email = authentication.getName();
        Account account = accountService
                .findAccountByEmail(email)
                .orElseThrow(()->new AccountNotFoundException("couldn't find account with email " + email)
                );
        account.setName(updateAccountDto.name());
        account.setJobTitle(updateAccountDto.jobTitle());
        account.setPhone(updateAccountDto.phone());
        return new ResponseEntity<>(
                accountService
                        .findAccountByEmail(email)
                        .orElseThrow(()->new AccountNotFoundException("couldn't find account with email " + email)
                        ),
                HttpStatus.OK);
    }
}
