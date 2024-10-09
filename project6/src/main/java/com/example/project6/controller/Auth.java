package com.example.project6.controller;


import com.example.project6.Service.AuthenticationService;
import com.example.project6.dto.AuthenticationResponse;
import com.example.project6.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
    @Validated
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate() {
        return ResponseEntity.ok(authenticationService.authenticate());
    }

}
