package com.example.project6.util.entityAndDtoMappers;

import com.example.project6.dto.CreateAccountRequest;
import com.example.project6.dto.ProfileDto;
import com.example.project6.entity.Account;

public class AccountMapper {

    static public Account createAccountRequestToAccountEntity(CreateAccountRequest request){
        return Account
                .builder()
                .withEmail(request.email())
                .withPassword(request.password())
                .withRole(request.role())
                .withName(request.name())
                .build();
    }
    static public ProfileDto AccountEntityToAccountProfileDto(Account account){
        // converts account entity to profile dto.

        return new ProfileDto( account.getName(), account.getEmail(), account.getAccountUuid(), account.getRole());
    }
}
