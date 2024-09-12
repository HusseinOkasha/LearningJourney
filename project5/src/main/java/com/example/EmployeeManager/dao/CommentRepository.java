package com.example.EmployeeManager.dao;

import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.model.Comment;
import com.example.EmployeeManager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>{
    @Query("SELECT c FROM Comment c  JOIN c.createdBy a JOIN a.tasks t WHERE c.uuid = :uuid AND a = :account AND t = :task")
    Optional<Comment> findByUuidAndAccountAndTask(@Param("uuid") UUID uuid,
                                                  @Param("account") Account account,
                                                  @Param("task") Task task);

    Optional<Comment> findByUuid(UUID uuid);
}
