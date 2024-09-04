package com.example.EmployeeManager.dao;

import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByUuid(UUID uuid);
    List<Account> findAllByRole(Role role);
    @Transactional
    void deleteByUuidAndRole(UUID uuid, Role role);
}
