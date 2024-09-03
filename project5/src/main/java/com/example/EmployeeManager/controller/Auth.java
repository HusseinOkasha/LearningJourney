package com.example.EmployeeManager.controller;

import com.example.EmployeeManager.dto.AuthenticationRequest;
import com.example.EmployeeManager.dto.AuthenticationResponse;
import com.example.EmployeeManager.dto.RegisterRequest;
import com.example.EmployeeManager.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class Auth {

    private final AuthenticationService authenticationService;

    @Autowired
    public Auth(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate() {
        return ResponseEntity.ok(authenticationService.authenticate());
    }

}
