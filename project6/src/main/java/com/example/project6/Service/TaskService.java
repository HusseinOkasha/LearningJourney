package com.example.project6.Service;


import com.amazonaws.services.dynamodbv2.datamodeling.TransactionWriteRequest;
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


import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final AccountService accountService;
    private final TaskAccountsService taskAccountsService;
    private final AccountTasksService accountTasksService;
    private final TransactionsRepository transactionsRepository;

    public TaskService(TaskRepository taskRepository,
                       AccountService accountService,
                       TaskAccountsService taskAccountsService,
                       TransactionsRepository transactionsRepository,
                       AccountTasksService accountTasksService) {

        this.taskRepository = taskRepository;
        this.accountService = accountService;
        this.taskAccountsService = taskAccountsService;
        this.accountTasksService = accountTasksService;
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
        return taskRepository
                .load(task)
                .orElseThrow( // throws exception in case the task isn't found.
                        ()-> new NotFoundException("There is no task with uuid: " + taskUuid)
                );
    }

    public void updateTaskByTaskUuid(Task task, UUID taskUuid){
        /*
        * It tries to update all task attributes.
        *   1) Fetches the task to be updated from the database, set its attributes to the updated values.
        *   2) Fetches task account links from the database,
        *       then update common attributes between task and task account link.
        *   3) Fetches account task links from the database,
        *       then update common attributes between task and account task link.
        *   4) Creates a write transaction to save the updates in Task to the database,
        *        TaskAccountLinks and AccountTaskLinks.
        * */

        // fetch the task from the database.
        Task dbTask = this.getTaskByUuid(taskUuid);

        // update the dbTask.
        dbTask.setDescription(task.getDescription());
        dbTask.setStatus(task.getStatus());
        dbTask.setTitle(task.getTitle());

        // fetch task account links from the database.
        List<TaskAccountLink> taskAccountLinks = taskAccountsService.getTaskAccounts(taskUuid);

        // update common attributes between task entity and TaskAccountLink entity.
        taskAccountLinks.forEach(
                (taskAccountLink)->
                        taskAccountLink.setTaskTitle(task.getTitle()) // update taskTitle in TaskAccountLink.
        );

        // fetch all AccountTaskLinks from the database.
        List<AccountTaskLink> accountTaskLinks = taskAccountLinks
                .stream()
                .map(
                        (taskAccountLink)->
                                accountTasksService
                                        .getByAccountUuidAndTaskUuid(
                                                taskAccountLink.getAccountUuid(), taskUuid
                                        )
                )
                .toList();

        // update common attributes between task entity and account task link entity.
        accountTaskLinks.forEach(
                accountTaskLink ->
                        accountTaskLink.setTaskTitle(task.getTitle()) // update task title in account task link.
        );

        // create write transaction request.
        TransactionWriteRequest transactionWriteRequest = new TransactionWriteRequest();

        // add the task it's self to be saved inside the transaction.
        transactionWriteRequest.addPut(dbTask);

        // add task account links to be saved inside the transaction.
        taskAccountLinks.forEach(transactionWriteRequest::addPut);

        // add account task links to be saved inside the transaction.
        accountTaskLinks.forEach(transactionWriteRequest::addPut);

        // fire the transaction to the database
        transactionsRepository.transactionWrite(transactionWriteRequest);
    }

    public void deleteTaskByUuid(UUID taskUuid) {
        // task to be deleted.
        Task task = Task
                .builder()
                .withTaskUuid(taskUuid)
                .build();

        // fetch all task account links from database.
        List<TaskAccountLink> taskAccounts = taskAccountsService.getTaskAccounts(taskUuid);

        // create write transaction request.
        TransactionWriteRequest transactionWriteRequest = new TransactionWriteRequest();

        // delete the task itself.
        transactionWriteRequest.addDelete(task);

        // delete links
        taskAccounts.forEach((link)-> {
            // delete task account link.
            transactionWriteRequest.addDelete(link);

            AccountTaskLink accountTaskLink = AccountTaskLink.builder()
                    .withAccountUuid(link.getAccountUuid())
                    .withTaskUuid(taskUuid)
                    .build();
            // delete account task link.
            transactionWriteRequest.addDelete(accountTaskLink);

        });
        transactionsRepository.transactionWrite(transactionWriteRequest);

    }
}
