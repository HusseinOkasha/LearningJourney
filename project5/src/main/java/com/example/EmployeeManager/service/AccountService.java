package com.example.EmployeeManager.service;

import com.example.EmployeeManager.dao.AccountRepository;
import com.example.EmployeeManager.exception.AccountNotFoundException;
import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.model.Role;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account addAccount(Account account) {
        account.setAccountCode(UUID.randomUUID().toString());
        return accountRepository.save(account);
    }

    public List<Account> findAllAccounts() {
        return accountRepository.findAll();
    }

    public Account updateAccount(Account account) {
        return accountRepository.save(account);
    }

    public Account findAccountById(Long id) {
        return accountRepository
                .findById(id)
                .orElseThrow(
                        () -> new AccountNotFoundException(
                                String.format("%s%s%s", "account with id ", id, " not found")
                        )
                );
    }
    public List<Account> findAllByRole(Role role){
        return  accountRepository.findAllByRole(role);
    }

    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }

}
