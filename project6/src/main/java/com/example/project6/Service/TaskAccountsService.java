package com.example.project6.Service;

import com.example.project6.dao.TaskAccountsRepository;
import com.example.project6.dao.TaskRepository;
import com.example.project6.entity.Account;
import com.example.project6.entity.Task;
import com.example.project6.entity.TaskAccountLink;
import com.example.project6.exception.NotFoundException;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;

import java.util.List;
import java.util.UUID;

@Service
public class TaskAccountsService {
    private final TaskAccountsRepository taskAccountsRepository;
    private final TaskRepository taskRepository;
    private final AccountService accountService;

    public TaskAccountsService(TaskAccountsRepository taskAccountsRepository,
                               TaskRepository taskRepository,
                               AccountService accountService) {
        this.taskAccountsRepository = taskAccountsRepository;
        this.taskRepository = taskRepository;
        this.accountService = accountService;
    }

    public List<TaskAccountLink> getTaskAccounts(UUID taskUuid) {
        return taskAccountsRepository.getTaskAccounts(taskUuid);
    }

    public void save(TaskAccountLink taskAccountLink){
        taskAccountsRepository.save(taskAccountLink);
    }
    public void shareTaskWithAccount(UUID taskUuid, UUID accountUuid){
        // fetch the task from the database.
        Task dbTask = taskRepository.load(Task.builder().withTaskUuid(taskUuid).build()).orElseThrow(
                ()-> new NotFoundException(String.format("couldn't find task with uuid: %s", taskUuid))
        );

        // fetch the account from the database.
        Account dbAccount = accountService.getAccountByUuid(accountUuid);

        // build the task account link.
        TaskAccountLink taskAccountLink = TaskAccountLink
                .builder()
                .withTaskUuid(dbTask.getTaskUuid())
                .withAccountUuid(dbAccount.getAccountUuid())
                .withAccountName(dbAccount.getName())
                .withTaskTitle(dbTask.getTitle())
                .build();

        // save the task account link to the database.
        taskAccountsRepository.save(taskAccountLink);
    }
    public TaskAccountLink getByTaskUuidAndAccountUuid(UUID taskUuid, UUID accountUuid){
        return taskAccountsRepository.load(
                TaskAccountLink
                        .builder()
                        .withTaskUuid(taskUuid)
                        .withAccountUuid(accountUuid)
                        .build()
        ).orElseThrow(()-> new NotFoundException(
                String.format("task with uuid: %s, isn't shared with account with uuid: %s", taskUuid, accountUuid)
                )
        );
    }

    public void unShareTaskWithAccount(UUID taskUuid, UUID accountUuid){
       // fetch the task account link from the database.
        TaskAccountLink taskAccountLink = getByTaskUuidAndAccountUuid(taskUuid, accountUuid);

        // delete the task account link from the database.
        taskAccountsRepository.delete(taskAccountLink);
    }



    public TransactWriteItem generatePutTransactWriteItem(TaskAccountLink taskAccountLink){
        return taskAccountsRepository.generatePutTransactWriteItem(taskAccountLink);
    }

    public TransactWriteItem generateDeleteTransactWriteItem(TaskAccountLink taskAccountLink){
        return taskAccountsRepository.generateDeleteTransactWriteItem(taskAccountLink);
    }
}
