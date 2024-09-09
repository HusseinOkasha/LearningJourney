package com.example.EmployeeManager.dao;

import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByUuid(UUID uuid);

    @Query("SELECT t FROM Account a JOIN a.tasks t WHERE a = :account AND t.uuid = :uuid")
    Optional<Task> findTaskByUuidAndAccount(@Param("uuid") UUID uuid, @Param("account") Account account);

    @Transactional
    void deleteByUuid(UUID uuid);
}
