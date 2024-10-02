package com.example.project6.controller;

import com.example.project6.Service.AccountService;
import com.example.project6.dto.CreateAccountRequest;
import com.example.project6.entity.Account;


import com.example.project6.util.entityAndDtoMappers.AccountMapper;
import com.example.project6.util.entityAndDtoMappers.DBItemMapper;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public Account createAccount(@RequestBody CreateAccountRequest request){
        // convert CreateAccountRequest dto to Account.
        Account account = AccountMapper.createAccountRequestToAccountEntity(request);
        return  accountService.save(account);
    }

    @GetMapping("/{accountUuid}")
    public Account getAccountByUuid(@PathVariable UUID accountUuid){
        return accountService.getAccountByUuid(accountUuid);
    }
}
