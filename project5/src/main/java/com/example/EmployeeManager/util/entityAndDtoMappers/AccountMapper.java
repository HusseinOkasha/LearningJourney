package com.example.EmployeeManager.util.entityAndDtoMappers;

import com.example.EmployeeManager.dto.AddAccountRequest;
import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.model.Role;

public class AccountMapper {

    static public Account addAccountRequestToAccountEntity(AddAccountRequest addAccountRequest){
        // converts "AddAccountRequest" to "Account"
        return Account.builder()
                .withEmail(addAccountRequest.email())
                .withPassword(addAccountRequest.password())
                .withJobTitle(addAccountRequest.jobTitle())
                .withName(addAccountRequest.name())
                .withPhone(addAccountRequest.phone())
                .withRole(addAccountRequest.role())
                .build();
    }
}
