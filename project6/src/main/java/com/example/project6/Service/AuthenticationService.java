package com.example.project6.Service;

import com.example.project6.dto.AuthenticationResponse;
import com.example.project6.dto.RegisterRequest;
import com.example.project6.entity.Account;
import com.example.project6.exception.NotFoundException;
import com.example.project6.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class AuthenticationService {


    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AccountService accountService;
    private final JwtService jwtService;


    @Autowired
    public AuthenticationService(BCryptPasswordEncoder bCryptPasswordEncoder,
                                 AccountService accountService,
                                 JwtService jwtService
    ) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.accountService = accountService;
        this.jwtService = jwtService;
    }

    public AuthenticationResponse register(RegisterRequest request) {



        Account account =Account.builder()
                .withEmail(request.email())
                .withName(request.name())
                .withPassword(request.password())
                .withRole(request.role())
                .build();

        Account savedAccount = accountService.save(account);


        String jwtToken = jwtService.generateToken(null, account.getEmail());

        return new AuthenticationResponse(jwtToken);
    }

    public AuthenticationResponse authenticate() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return new AuthenticationResponse(
                jwtService.generateToken(
                        Map.of("roles", auth.getAuthorities()),
                        auth.getName()
                )
        );
    }

    public Account getAuthenticatedAccount(){
        /*
        * It returns the currently authenticated (ADMIN / EMPLOYEE) accounts'.
        * */

        // fetch the authentication object.
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // extract the accountUuid from the authentication object.
        UUID accountUuid = userDetails.getAccountUuid();

        // fetch the account corresponding to the extracted email
        return accountService.getAccountByUuid(accountUuid);
    }
}
