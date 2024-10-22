package com.example.project6.Service;

import com.example.project6.dao.AccountTasksRepository;
import com.example.project6.dao.TaskAccountsRepository;
import com.example.project6.dao.TaskRepository;
import com.example.project6.dao.TransactionsRepository;
import com.example.project6.entity.Account;
import com.example.project6.entity.AccountTaskLink;
import com.example.project6.entity.Task;
import com.example.project6.entity.TaskAccountLink;
import com.example.project6.exception.NotFoundException;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class TaskAccountsService {
    private final TaskAccountsRepository taskAccountsRepository;
    private final TaskRepository taskRepository;
    private final AccountService accountService;
    private final AccountTasksRepository accountTasksRepository;
    private final TransactionsRepository transactionsRepository;
    public TaskAccountsService(TaskAccountsRepository taskAccountsRepository,
                               TaskRepository taskRepository,
                               AccountService accountService, AccountTasksRepository accountTasksRepository, TransactionsRepository transactionsRepository) {
        this.taskAccountsRepository = taskAccountsRepository;
        this.taskRepository = taskRepository;
        this.accountService = accountService;
        this.accountTasksRepository = accountTasksRepository;
        this.transactionsRepository = transactionsRepository;
    }

    public List<TaskAccountLink> getTaskAccounts(UUID taskUuid) {
        return taskAccountsRepository.getTaskAccounts(taskUuid);
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

        // build the account task link (to handle the other side of the relation).
        AccountTaskLink accountTaskLink = AccountTaskLink
                .builder()
                .withTaskUuid(dbTask.getTaskUuid())
                .withAccountUuid(dbAccount.getAccountUuid())
                .withAccountName(dbAccount.getName())
                .withTaskTitle(dbTask.getTitle())
                .build();

        // get write transaction item for the task account link.
        List<TransactWriteItem> transactWriteItems = Stream.of(
                generatePutTransactWriteItem(taskAccountLink),
                        accountTasksRepository.generatePutTransactWriteItem(accountTaskLink)
                )
                .toList();
        TransactWriteItemsRequest transactWriteItemsRequest = TransactWriteItemsRequest.builder().transactItems(
                transactWriteItems
        ).build();
        transactionsRepository.transactionWrite(transactWriteItemsRequest);
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
        AccountTaskLink accountTaskLink = accountTasksRepository.getByAccountUuidAndTaskUuid(accountUuid, taskUuid)
                .orElseThrow(()-> new NotFoundException(
                        String.format("couldn't find account task link for accountUuid: %s, and taskUuid: %s",
                                accountUuid, taskUuid)
                ));

        // get write transaction item for the task account link.
        List<TransactWriteItem> transactWriteItems = Stream.of(
                        generateDeleteTransactWriteItem(taskAccountLink),
                        accountTasksRepository.generateDeleteTransactWriteItem(accountTaskLink)
                )
                .toList();

        TransactWriteItemsRequest transactWriteItemsRequest = TransactWriteItemsRequest.builder().transactItems(
                transactWriteItems
        ).build();

        // fire the transaction.
        transactionsRepository.transactionWrite(transactWriteItemsRequest);
    }



    public TransactWriteItem generatePutTransactWriteItem(TaskAccountLink taskAccountLink){
        return taskAccountsRepository.generatePutTransactWriteItem(taskAccountLink);
    }

    public TransactWriteItem generateDeleteTransactWriteItem(TaskAccountLink taskAccountLink){
        return taskAccountsRepository.generateDeleteTransactWriteItem(taskAccountLink);
    }
}
