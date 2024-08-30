package com.example.EmployeeManager.service;

import com.example.EmployeeManager.dao.EmployeeRepository;
import com.example.EmployeeManager.dto.AuthenticationRequest;
import com.example.EmployeeManager.dto.AuthenticationResponse;
import com.example.EmployeeManager.dto.RegisterRequest;
import com.example.EmployeeManager.model.Employee;
import com.example.EmployeeManager.model.EmployeeBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthenticationService {


    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmployeeRepository employeeRepository;
    private final JwtService jwtService;


    @Autowired
    public AuthenticationService(BCryptPasswordEncoder bCryptPasswordEncoder,
                                 EmployeeRepository employeeRepository,
                                 JwtService jwtService
    ) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.employeeRepository = employeeRepository;
        this.jwtService = jwtService;
    }

    public AuthenticationResponse register(RegisterRequest request) {

        EmployeeBuilder employeeBuilder = new EmployeeBuilder();

        Employee employee = employeeBuilder
                .withEmail(request.email())
                .withName(request.name())
                .withPassword(bCryptPasswordEncoder.encode(request.password()))
                .withRole(request.role())
                .build();

        Employee savedEmployee = employeeRepository.save(employee);

        String jwtToken = jwtService.generateToken(null, savedEmployee);

        return new AuthenticationResponse(jwtToken);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        // get the employee from the database.
        Employee employee = employeeRepository
               .findByEmail(request.email())
               .orElseThrow(()-> new BadCredentialsException("email and password doesn't match."));

        return new AuthenticationResponse(
                jwtService.generateToken(
                        Map.of("roles", employee.getAuthorities()),
                        employee
                )
        );
    }
}
