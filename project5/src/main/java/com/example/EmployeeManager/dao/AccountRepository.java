package com.example.EmployeeManager.dao;

import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);

    List<Account> findAllByRole(Role role);
}
