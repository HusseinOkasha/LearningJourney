package com.example.project6.Service;

import com.example.project6.entity.Account;
import com.example.project6.security.CustomUserDetails;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final AccountService accountService;

    public UserDetailsService(AccountService accountService) {
        this.accountService = accountService;
    }


    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountService.getAccountByEmail(username);
        return new CustomUserDetails(account);

    }
}
