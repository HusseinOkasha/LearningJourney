package com.example.EmployeeManager.service;


import com.example.EmployeeManager.dao.EmployeeRepository;
import com.example.EmployeeManager.exception.EmployeeNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class EmployeeDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeDetailsService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return employeeRepository.findByEmail(username).orElseThrow(()-> new EmployeeNotFoundException("" +
                "Couldn't find employee with email: " + username)
        );
    }
}
