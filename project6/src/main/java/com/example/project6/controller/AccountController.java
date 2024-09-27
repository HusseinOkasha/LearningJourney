package com.example.project6.controller;

import com.example.project6.Service.AccountService;
import com.example.project6.dto.CreateAccountRequest;
import com.example.project6.entity.DBItem;

import com.example.project6.util.entityAndDtoMappers.DBItemMapper;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public DBItem createAccount(@RequestBody CreateAccountRequest request){

        // convert CreateAccountRequest dto to DBItem.
        DBItem item = DBItemMapper.createAccountRequestToDBItem(request);
        return  accountService.save(item);
    }

}
