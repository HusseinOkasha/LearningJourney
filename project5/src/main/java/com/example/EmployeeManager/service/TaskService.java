package com.example.EmployeeManager.service;

import com.example.EmployeeManager.dao.TaskRepository;
import com.example.EmployeeManager.exception.NotFoundException;
import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.model.Task;
import org.springframework.beans.NotReadablePropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskService {

    @Autowired
    private final AccountService accountService;
    @Autowired
    private final TaskRepository taskRepository;

    public TaskService(AccountService accountService, TaskRepository taskRepository) {
        this.accountService = accountService;
        this.taskRepository = taskRepository;
    }

    public Task findTaskByUuid(UUID uuid) {
        return this.taskRepository
                .findByUuid(uuid)
                .orElseThrow(
                        () -> new NotFoundException("couldn't find task with uuid: " + uuid )
                );
    }
    public Optional<Task> findTaskByUuidAndAccount(UUID uuid, Account account){
        return this.taskRepository.findTaskByUuidAndAccount(uuid, account);
    }

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public Task save(Task task) {
        return this.taskRepository.save(task);
    }
    public void deleteAll(){
        this.taskRepository.deleteAll();
    }

    public void deleteTaskByUuid(UUID uuid) {
        taskRepository.deleteByUuid(uuid);
    }

}
