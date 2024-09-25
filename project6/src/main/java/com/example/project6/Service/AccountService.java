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
        return  accountRepository.save(account);
    }

    public Account getAccountByPk(String pk) {
        return accountRepository.getAccountByPk(pk);
    }

    public void deleteAccountByPk(String pk) {
         accountRepository.deleteAccountByPk(pk);
    }

    public Account updateAccountByPk(String pk, Account account) {
        Account dbAccount = accountRepository.getAccountByPk(pk);
        dbAccount.setName(account.getName());
        dbAccount.setEmail(account.getEmail());
        accountRepository.save(dbAccount);
        return accountRepository.getAccountByPk(dbAccount.getPk());

    }
}
