package com.example.project6.controller;

import com.example.project6.Enum.Role;
import com.example.project6.Service.AccountService;
import com.example.project6.dto.CreateAccountRequest;
import com.example.project6.dto.ProfileDto;
import com.example.project6.entity.Account;
import com.example.project6.util.entityAndDtoMappers.AccountMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AccountService accountService;

    public AdminController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<ProfileDto> createAdminAccount(@Valid @RequestBody CreateAccountRequest createAccountRequest){
        /*
        * Handles HTTP POST requests to /api/admin
        * In case of successful creation it returns :
        *   - returns the created account profile dto.
        *   - HTTP STATUS CREATED (201).
        * */

        // convert create account request to Account entity.
        Account account = AccountMapper.createAccountRequestToAccountEntity(createAccountRequest);

        Account createdAccount = accountService.createAccount(account);

        // convert the created account into profile dto.
        ProfileDto profileDto = AccountMapper.AccountEntityToAccountProfileDto(createdAccount);

        return new ResponseEntity<ProfileDto>(profileDto, HttpStatus.CREATED);
    }

    @GetMapping("/{accountUuid}")
    public ResponseEntity<ProfileDto> getAdminAccountByUuid(@PathVariable UUID accountUuid){
        /*
        * Handle HTTP GET requests to /api/admin/{accountUuid}
        * In case of success it returns
        *   - profileDto
        *   - HTTP response status OK 200.
        * */

        Account DBAccount = accountService.getAccountByUuidAndRole(accountUuid, Role.ADMIN);

        // convert the account entity into profile dto.
        ProfileDto profileDto = AccountMapper.AccountEntityToAccountProfileDto(DBAccount);

        return new ResponseEntity<>(profileDto, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProfileDto>> getAllAdminAccounts(){
        /*
        * Handle HTTP GET requests  to /api/admin/all
        * In case of success it returns
        *   - profile dto.
        *   - HTTP response status OK 200.
        * */
        List<Account> admins = accountService.getAllAdmins();
        List<ProfileDto>adminProfilesDto = admins
                .stream()
                .map(AccountMapper::AccountEntityToAccountProfileDto)
                .toList();
        return new ResponseEntity<>(adminProfilesDto, HttpStatus.OK);

    }

}
