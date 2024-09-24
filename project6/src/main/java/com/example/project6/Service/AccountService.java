package com.example.project6.Service;

import com.example.project6.dao.AccountRepository;
import com.example.project6.entity.Account;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    final private AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public String createAccount(Account account){
        return  accountRepository.createAccount(account);
    }
}
