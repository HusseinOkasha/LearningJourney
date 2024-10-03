package com.example.project6.controller;

import com.example.project6.Enum.Role;
import com.example.project6.Service.AccountService;
import com.example.project6.dto.CreateAccountRequest;
import com.example.project6.dto.ProfileDto;
import com.example.project6.entity.Account;
import com.example.project6.util.entityAndDtoMappers.AccountMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/api/admin/employees")
@RestController
public class EmployeeController {
    private final AccountService accountService;


    public EmployeeController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ProfileDto createNewEmployee(@Valid @RequestBody CreateAccountRequest createAccountRequest){
        /*
        * Handles HTTP POST requests to "/api/admin/employees"
        * creates account with role employee.
        * returns the created account.
        * */
        Account createdAccount = accountService
                .createAccount(
                        // converts createAccountRequest to Account entity.
                        AccountMapper.createAccountRequestToAccountEntity(createAccountRequest)
                );

        // converts Account entity to  account profile dto.
        return AccountMapper.AccountEntityToAccountProfileDto(createdAccount);
    }

    @GetMapping("/{accountUuid}")
    public ProfileDto getEmployeeByUuid(@PathVariable UUID accountUuid){
        return AccountMapper
                .AccountEntityToAccountProfileDto(
                        accountService.getAccountByUuidAndRole(accountUuid, Role.EMPLOYEE)
                );
    }


}
