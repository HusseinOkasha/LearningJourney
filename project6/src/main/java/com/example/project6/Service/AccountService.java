package com.example.project6.Service;

import com.example.project6.Enum.Role;
import com.example.project6.dao.AccountRepository;
import com.example.project6.entity.Account;
import com.example.project6.exception.NotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public AccountService(AccountRepository accountRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.accountRepository = accountRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public Account save(Account account){
        // generate account uuid.
        account.generateAccountUuid();

        // Encrypt the password before saving it to the database.
        String rawPassword = account.getPassword();
        account.setPassword(bCryptPasswordEncoder.encode(rawPassword));

        // save the password to the database.
        accountRepository.save(account);
        return account;
    }

    public Account getAccountByUuidAndRole(UUID uuid, Role role) {
        Account dbAccount = getAccountByUuid(uuid);
        // check that the account has the required role.
        if (dbAccount.getRole() != role){
            throw new NotFoundException(String.format("Couldn't find an %s with uuid: %s", role, uuid ));
        }
        return accountRepository.load(dbAccount);
    }

    public Account getAccountByUuid(UUID uuid) {
        Account account  = Account.builder().withAccountUuid(uuid).build();
        return accountRepository.load(account);
    }

    public Account getAccountByEmail(String email){
        return accountRepository
                .findByEmail(email)
                .orElseThrow(() -> new NotFoundException("couldn't find account with email: " + email));
    }

    public List<Account> getAllEmployees() {
        return accountRepository.getAllByRole(Role.EMPLOYEE);
    }

    public void deleteAccountByUuid(UUID accountUuid) {
        Account account = Account.builder().withAccountUuid(accountUuid).build();
        accountRepository.deleteByAccountUuid(account);
    }

    public List<Account> getAllAdmins() {
        return accountRepository.getAllByRole(Role.ADMIN);
    }
}
