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

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/admin/employees")
@RestController
public class EmployeeController {
    private final AccountService accountService;


    public EmployeeController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<ProfileDto> createNewEmployee(@Valid @RequestBody CreateAccountRequest createAccountRequest){
        /*
        * Handles HTTP POST requests to "/api/admin/employees"
        * creates account with role employee.
        * returns the created account.
        * */
        Account createdAccount = accountService
                .save(
                        // converts createAccountRequest to Account entity.
                        AccountMapper.createAccountRequestToAccountEntity(createAccountRequest)
                );

        // converts Account entity to  account profile dto.
        return new ResponseEntity<>(AccountMapper.AccountEntityToAccountProfileDto(createdAccount), HttpStatus.CREATED);
    }

    @GetMapping("/{accountUuid}")
    public ResponseEntity<ProfileDto> getEmployeeByUuid(@PathVariable UUID accountUuid){
        /*
        * Handles HTTP GET requests to "/api/admin/employees/{accountUuid}"
        * In case of success, it returns:
        *   - profile dto of the account.
        *   - HTTP STATUS CODE 200 OK.
        * */
        return new ResponseEntity<>(AccountMapper
                .AccountEntityToAccountProfileDto(
                        accountService.getAccountByUuidAndRole(accountUuid, Role.EMPLOYEE)
                ), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProfileDto>> getAllEmployees(){
        List<Account> employees = accountService.getAllEmployees();
        // convert the accounts to profile dto.
        List<ProfileDto> employeesProfileDto = employees
                .stream()
                .map(AccountMapper::AccountEntityToAccountProfileDto)
                .toList();
        return new ResponseEntity<>(employeesProfileDto, HttpStatus.OK);
    }

    @DeleteMapping("/{accountUuid}")
    public ResponseEntity DeleteEmployeeByUuid(@PathVariable UUID accountUuid){
        accountService.deleteAccountByUuid(accountUuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
