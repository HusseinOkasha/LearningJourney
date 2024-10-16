package com.example.EmployeeManager.service;

import com.example.EmployeeManager.dao.AccountRepository;
import com.example.EmployeeManager.exception.NotFoundException;
import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.model.Role;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public AccountService(AccountRepository accountRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.accountRepository = accountRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public Account addAccount(Account account) {

        // encode the raw password
        String rawPassword = account.getPassword();
        account.setPassword(bCryptPasswordEncoder.encode(rawPassword));

        // save the account to the database.
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
                        () -> new NotFoundException(
                                String.format("%s%s%s", "account with id ", id, " not found")
                        )
                );
    }
    public List<Account> findAllByRole(Role role){
        return  accountRepository.findAllByRole(role);
    }
    public Account save(Account account){
        return this.accountRepository.save(account);
    }
    public void deleteAll(){
        accountRepository.deleteAll();
    }
    public Account findByUuid(UUID uuid){
        return accountRepository
                .findByUuid(uuid)
                .orElseThrow(
                        () -> new NotFoundException("Couldn't find an account with uuid: "+ uuid )
                );
    }

    public Optional<Account> findAccountByEmail(String email){
        return accountRepository.findByEmail(email);
    }
    public void deleteAccountByUuidAndRole(UUID uuid, Role role) {
        accountRepository.deleteByUuidAndRole(uuid, role);
    }

}
