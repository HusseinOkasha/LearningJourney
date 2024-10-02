package com.example.project6.Service;

import com.example.project6.dao.AccountRepository;
import com.example.project6.entity.Account;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccountService {
    final private AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account save(Account account){
        // generate uuid for the account if it doesn't have one.
        if(account.getAccountUuid() == null){
            account.setAccountUuid(UUID.randomUUID());
        }
        accountRepository.save(account);
        return account;
    }

    public Account getAccountByUuid(UUID uuid) {
        Account account  = Account.builder().withAccountUuid(uuid).build();
        return accountRepository.load(account);
    }
}
