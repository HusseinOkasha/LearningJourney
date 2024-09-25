package com.example.project6.controller;

import com.example.project6.Service.AccountService;
import com.example.project6.entity.Account;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping()
    public String createAccount(@RequestBody Account account){
        return  accountService.createAccount(account);
    }

    @GetMapping("/{pk}")
    public Account getAccountByPk(@PathVariable String pk){
        return accountService.getAccountByPk(pk);
    }

    @PutMapping("/{pk}")
    public Account updateAccountByPk(@PathVariable String pk, @RequestBody Account account){
        return accountService.updateAccountByPk(pk, account);
    }

    @DeleteMapping("/{pk}")
    public void deleteAccountByPk(@PathVariable String pk){
        accountService.deleteAccountByPk(pk);
    }
}
