package com.example.project6.Service;


import com.amazonaws.services.dynamodbv2.datamodeling.TransactionWriteRequest;
import com.example.project6.dao.AccountTaskRepository;
import com.example.project6.dao.TaskAccountLinkRepository;
import com.example.project6.dao.TaskRepository;
import com.example.project6.dao.TransactionsRepository;
import com.example.project6.entity.Account;
import com.example.project6.entity.AccountTaskLink;
import com.example.project6.entity.Task;
import com.example.project6.entity.TaskAccountLink;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final AccountService accountService;
    private final TransactionsRepository transactionsRepository;

    public TaskService(TaskRepository taskRepository,
                       AccountService accountService,
                       TransactionsRepository transactionsRepository) {

        this.taskRepository = taskRepository;
        this.accountService = accountService;
        this.transactionsRepository = transactionsRepository;
    }

    public Task createNewTask(Task task){
        if(task.getTaskUuid() == null){
            task.setTaskUuid(UUID.randomUUID());
        }
        return taskRepository.save(task);
    }

    public void addTaskToAccount(UUID accountUuid, Task task){
        // fetch the account from the database.
        Account dbAccount = accountService.getAccountByUuid(accountUuid);

        // create task uuid if it isn't created.
        if(task.getTaskUuid() == null){
            task.setTaskUuid(UUID.randomUUID());
        }

        UUID taskUuid = task.getTaskUuid();

        // create write transaction request.
        TransactionWriteRequest transactionWriteRequest = new TransactionWriteRequest();

        // add saving the task to the transaction.
        transactionWriteRequest.addPut(task);

        // create Account_Task_Link
        AccountTaskLink accountTaskLink = AccountTaskLink.builder()
                .withAccountUuid(accountUuid)
                .withTaskUuid(taskUuid)
                .withAccountName(dbAccount.getName())
                .withTaskTitle(task.getTitle())
                .build();

        // add saving Account_Task_Link to the transaction.
        transactionWriteRequest.addPut(accountTaskLink);

        // create Task_Account_Link
        TaskAccountLink taskAccountLink = TaskAccountLink.builder()
                .withPk(String.format("TASK#%s", taskUuid))
                .withSk(String.format("ACCOUNT#%s", accountUuid))
                .withAccountUuid(accountUuid)
                .withTaskUuid(taskUuid)
                .withTaskTitle(dbAccount.getName())
                .build();

        // add saving Task_Account_Link to the transaction.
        transactionWriteRequest.addPut(taskAccountLink);

        // perform the transaction on the database.
        transactionsRepository.transactionWrite(transactionWriteRequest);
    }

    public Task getTaskByUuid(UUID taskUuid) {
        Task task = Task.builder().withTaskUuid(taskUuid).build();
        return taskRepository.load(task);
    }


}
