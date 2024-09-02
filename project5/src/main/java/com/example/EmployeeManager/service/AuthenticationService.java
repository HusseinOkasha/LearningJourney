package com.example.EmployeeManager.service;

import com.example.EmployeeManager.dao.AccountRepository;
import com.example.EmployeeManager.dto.AuthenticationRequest;
import com.example.EmployeeManager.dto.AuthenticationResponse;
import com.example.EmployeeManager.dto.RegisterRequest;
import com.example.EmployeeManager.model.Account;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthenticationService {


    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AccountRepository accountRepository;
    private final JwtService jwtService;


    @Autowired
    public AuthenticationService(BCryptPasswordEncoder bCryptPasswordEncoder,
                                 AccountRepository accountRepository,
                                 JwtService jwtService
    ) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.accountRepository = accountRepository;
        this.jwtService = jwtService;
    }

    public AuthenticationResponse register(RegisterRequest request) {



        Account account =Account.builder()
                .withEmail(request.email())
                .withName(request.name())
                .withPassword(bCryptPasswordEncoder.encode(request.password()))
                .withRole(request.role())
                .build();

        Account savedAccount = accountRepository.save(account);


        String jwtToken = jwtService.generateToken(null, account);

        return new AuthenticationResponse(jwtToken);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        // get the account from the database.
        Account account = accountRepository
               .findByEmail(request.email())
               .orElseThrow(()-> new BadCredentialsException("email and password doesn't match."));

        return new AuthenticationResponse(
                jwtService.generateToken(
                        Map.of("roles", account.getAuthorities()),
                        account
                )
        );
    }
}
