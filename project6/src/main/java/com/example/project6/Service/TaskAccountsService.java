package com.example.project6.Service;

import com.example.project6.dao.TaskAccountsRepository;
import com.example.project6.entity.TaskAccountLink;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;

import java.util.List;
import java.util.UUID;

@Service
public class TaskAccountsService {
    private final TaskAccountsRepository taskAccountsRepository;

    public TaskAccountsService(TaskAccountsRepository taskAccountsRepository) {
        this.taskAccountsRepository = taskAccountsRepository;
    }

    public List<TaskAccountLink> getTaskAccounts(UUID taskUuid) {
        return taskAccountsRepository.getTaskAccounts(taskUuid);
    }

    public TransactWriteItem generatePutTransactWriteItem(TaskAccountLink taskAccountLink){
        return taskAccountsRepository.generatePutTransactWriteItem(taskAccountLink);
    }

    public TransactWriteItem generateDeleteTransactWriteItem(TaskAccountLink taskAccountLink){
        return taskAccountsRepository.generateDeleteTransactWriteItem(taskAccountLink);
    }
}
