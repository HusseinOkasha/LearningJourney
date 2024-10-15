package com.example.project6.Service;


import com.example.project6.Enum.TaskStatus;
import com.example.project6.dao.TaskRepository;
import com.example.project6.dao.TransactionsRepository;
import com.example.project6.entity.Account;
import com.example.project6.entity.AccountTaskLink;
import com.example.project6.entity.Task;
import com.example.project6.entity.TaskAccountLink;
import com.example.project6.exception.NotFoundException;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.dynamodb.model.*;


import java.util.*;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final AccountService accountService;
    private final AuthenticationService authenticationService;
    private final TaskAccountsService taskAccountsService;
    private final AccountTasksService accountTasksService;
    private final TransactionsRepository transactionsRepository;
    private final DynamoDbTable<AccountTaskLink> accountTaskLinkTable;
    private final DynamoDbTable<Task> taskTable;
    private final DynamoDbTable<TaskAccountLink> taskAccountLinkTable;

    public TaskService(TaskRepository taskRepository,
                       AccountService accountService, AuthenticationService authenticationService,
                       TaskAccountsService taskAccountsService,
                       TransactionsRepository transactionsRepository,
                       AccountTasksService accountTasksService, DynamoDbTable<AccountTaskLink> accountTaskLinkTable,
                       DynamoDbTable<Task> taskTable, DynamoDbTable<TaskAccountLink> taskAccountLinkTable) {

        this.taskRepository = taskRepository;
        this.accountService = accountService;
        this.authenticationService = authenticationService;
        this.taskAccountsService = taskAccountsService;
        this.accountTasksService = accountTasksService;
        this.transactionsRepository = transactionsRepository;

        this.accountTaskLinkTable = accountTaskLinkTable;
        this.taskTable = taskTable;
        this.taskAccountLinkTable = taskAccountLinkTable;
    }

    public Task createNewTask(Task task) {
        if (task.getTaskUuid() == null) {
            task.setTaskUuid(UUID.randomUUID());
        }
        return taskRepository.save(task);
    }

    public void addTaskToAccount(Task task) {
        // get the currently authenticated account.
        Account account = authenticationService.getAuthenticatedAccount();

        // create task uuid if it isn't created.
        if (task.getTaskUuid() == null) {
            task.setTaskUuid(UUID.randomUUID());
        }

        // get the task uuid.
        UUID taskUuid = task.getTaskUuid();

        // Convert the Task object into a DynamoDB-compatible map format.
        // The second argument (false) indicates that fields with null values in the Task object
        // should be excluded from the resulting map. This ensures that only non-null fields
        // are included when storing the item in DynamoDB, as DynamoDB does not store attributes with null values.
        // The map is then used to create a Put request for adding the Task item to the "app" table.
        Put addTask = Put.builder()
                .tableName("app")
                .item(taskTable.tableSchema().itemToMap(task, false))
                .build();

        // create Account_Task_Link
        AccountTaskLink accountTaskLink = AccountTaskLink.builder()
                .withAccountUuid(account.getAccountUuid())
                .withTaskUuid(taskUuid)
                .withAccountName(account.getName())
                .withTaskTitle(task.getTitle())
                .build();

        // Convert the AccountTaskLink object into a DynamoDB-compatible map format.
        // The second argument (false) indicates that fields with null values in the Task object
        // should be excluded from the resulting map. This ensures that only non-null fields
        // are included when storing the item in DynamoDB, as DynamoDB does not store attributes with null values.
        // The map is then used to create a Put request for adding the Task item to the "app" table.
        Put addAccountTaskLink = Put
                .builder()
                .tableName("app")
                .item(accountTaskLinkTable.tableSchema().itemToMap(accountTaskLink, false))
                .build();

        // create Task_Account_Link
        TaskAccountLink taskAccountLink = TaskAccountLink.builder()
                .withPk(String.format("TASK#%s", taskUuid))
                .withSk(String.format("ACCOUNT#%s", account.getAccountUuid()))
                .withAccountUuid(account.getAccountUuid())
                .withAccountName(account.getName())
                .withTaskUuid(taskUuid)
                .withTaskTitle(task.getTitle())
                .build();

        // Convert the TaskAccountLink object into a DynamoDB-compatible map format.
        // The second argument (false) indicates that fields with null values in the Task object
        // should be excluded from the resulting map. This ensures that only non-null fields
        // are included when storing the item in DynamoDB, as DynamoDB does not store attributes with null values.
        // The map is then used to create a Put request for adding the Task item to the "app" table.
        Put addTaskAccountLink = Put
                .builder()
                .tableName("app")
                .item(taskAccountLinkTable.tableSchema().itemToMap(taskAccountLink, false))
                .build();


        // create write transaction request.
        TransactWriteItemsRequest transactWriteItemsRequest = TransactWriteItemsRequest.builder().transactItems(
                TransactWriteItem.builder().put(addTask).build(),
                TransactWriteItem.builder().put(addAccountTaskLink).build(),
                TransactWriteItem.builder().put(addTaskAccountLink).build()
        ).build();

        // perform the transaction on the database.
        transactionsRepository.transactionWrite(transactWriteItemsRequest);
    }

    public Task getTaskByUuid(UUID taskUuid) {
        Task task = Task.builder().withTaskUuid(taskUuid).build();
        return taskRepository
                .load(task)
                .orElseThrow( // throws exception in case the task isn't found.
                        () -> new NotFoundException("There is no task with uuid: " + taskUuid)
                );
    }

    public void updateTaskByTaskUuid(Task task, UUID taskUuid) {
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

        // Fetch and Update (task account link) & (account task link).
        List<TaskAccountLink> taskAccountLinks = fetchAndUpdateTaskAccountLinks(dbTask);
        List<AccountTaskLink> accountTaskLinks = fetchAndUpdateAccountTaskLinks(taskAccountLinks, dbTask);

        // create task transact item.
        TransactWriteItem updateTaskEntity = TransactWriteItem
                .builder()
                .put(Put.builder()
                        .tableName("app")
                        .item(taskTable.tableSchema().itemToMap(dbTask, false))
                        .build()
                )
                .build();

        // create account task links transact items.
        List<TransactWriteItem> updateAccountTasksTransactItems =
                getTransactWriteItemsFromTaskAccountLinks(taskAccountLinks);

        // create task account links transact items.
        List<TransactWriteItem> updateTaskAccountTransactItems =
                getTransactWriteItemsFromAccountTaskLinks(accountTaskLinks);

        // create a list of all transact items (task, task account links, and account task links).
        List<TransactWriteItem> transactWriteItems = new ArrayList<>();
        transactWriteItems.addAll(updateAccountTasksTransactItems);
        transactWriteItems.addAll(updateTaskAccountTransactItems);
        transactWriteItems.add(updateTaskEntity);

        // create the transaction write request.
        TransactWriteItemsRequest transactWriteItemsRequest = TransactWriteItemsRequest.builder().transactItems(
                transactWriteItems
        ).build();

        // fire the transaction to the database
        transactionsRepository.transactionWrite(transactWriteItemsRequest);
    }

    public void updateTaskTitleByUuid(String title, UUID taskUuid) {
        /*
         * It tries to update task title by uuid.
         *   1) Fetches the task to be updated from the database, set the title to the updated title,
         *   2) Fetches task account links from the database,
         *       then updates taskTitle.
         *   3) Fetches account task links from the database,
         *       then updates the task title.
         *   4) Creates a write transaction to save the updates in Task to the database,
         *        TaskAccountLinks and AccountTaskLinks.
         * */

        // fetch the task from the database.
        Task dbTask = this.getTaskByUuid(taskUuid);

        // update the dbTask.
        dbTask.setTitle(title);

        // Fetch and Update (task account link) & (account task link).
        List<TaskAccountLink> taskAccountLinks = fetchAndUpdateTaskAccountLinks(dbTask);
        List<AccountTaskLink> accountTaskLinks = fetchAndUpdateAccountTaskLinks(taskAccountLinks, dbTask);

        // create task transact item.
        TransactWriteItem updateTaskEntity = TransactWriteItem
                .builder()
                .put(Put.builder()
                        .tableName("app")
                        .item(taskTable.tableSchema().itemToMap(dbTask, false))
                        .build()
                )
                .build();

        // create account task links transact items.
        List<TransactWriteItem> updateAccountTasksTransactItems =
                getTransactWriteItemsFromTaskAccountLinks(taskAccountLinks);

        // create task account links transact items.
        List<TransactWriteItem> updateTaskAccountTransactItems =
                getTransactWriteItemsFromAccountTaskLinks(accountTaskLinks);

        // create a list of all transact items (task, task account links, and account task links).
        List<TransactWriteItem> transactWriteItems = new ArrayList<>();
        transactWriteItems.addAll(updateAccountTasksTransactItems);
        transactWriteItems.addAll(updateTaskAccountTransactItems);
        transactWriteItems.add(updateTaskEntity);

        // create the transaction write request.
        TransactWriteItemsRequest transactWriteItemsRequest = TransactWriteItemsRequest.builder().transactItems(
                transactWriteItems
        ).build();

        // fire the transaction to the database
        transactionsRepository.transactionWrite(transactWriteItemsRequest);

    }

    public void updateTaskDescriptionByUuid(String description, UUID taskUuid) {
        /*
         * Update the task description using the task uuid.
         *   1) Fetches the task we want to update.
         *   2) Sets the tasks description with the new description.
         *   3) Save the task after performing the update to the database.
         * */


        // fetch the task from the database.
        Task dbTask = this.getTaskByUuid(taskUuid);

        // update the db task description.
        dbTask.setDescription(description);

        // save the task to the database.
        this.taskRepository.save(dbTask);

    }


    public void updateTaskStatusByUuid(TaskStatus status, UUID taskUuid) {
        /*
         * Updates the task status using task uuid.
         *   1) Fetches the task we want to update.
         *   2) Sets the task status with the new status.
         *   3) Save the task after performing the update to the database.
         * */

        // fetch the task from the database.
        Task dbTask = this.getTaskByUuid(taskUuid);

        // update the dbTask status.
        dbTask.setStatus(status);

        // save the task to the database.
        this.taskRepository.save(dbTask);
    }

    public void deleteTaskByUuid(UUID taskUuid) {
        // task to be deleted.
        Task task = Task
                .builder()
                .withTaskUuid(taskUuid)
                .build();

        // fetch all task account links from database.
        List<TaskAccountLink> taskAccounts = taskAccountsService.getTaskAccounts(taskUuid);


        Map<String, AttributeValue> taskEntityKey = Map.of("pk", AttributeValue.builder().s(task.getPk()).build(),
                "sk", AttributeValue.builder().s(task.getSk()).build());

        TransactWriteItem deleteTaskWriteTransactionItem = TransactWriteItem
                .builder()
                .delete(
                        Delete
                                .builder()
                                .tableName("app")
                                .key(taskEntityKey)
                                .build()
                ).build();

        List<TransactWriteItem> toBeDeleted = new ArrayList<>();
        toBeDeleted.add(deleteTaskWriteTransactionItem);

        // delete links
        taskAccounts.forEach((taskAccountLink) -> {
            // delete task account link.
            toBeDeleted.add(TransactWriteItem.builder().delete(
                            Delete.builder()
                                    .tableName("app")
                                    .key(Map.of("pk", AttributeValue.builder().s(taskAccountLink.getPk()).build(),
                                            "sk", AttributeValue.builder().s(taskAccountLink.getSk()).build()))
                                    .build()
                            )
                    .build()
            );

            AccountTaskLink accountTaskLink = AccountTaskLink.builder()
                    .withAccountUuid(taskAccountLink.getAccountUuid())
                    .withTaskUuid(taskUuid)
                    .build();

            // delete task account link.
            toBeDeleted.add(TransactWriteItem.builder().delete(
                                    Delete.builder()
                                            .tableName("app")
                                            .key(Map.of("pk", AttributeValue.builder().s(accountTaskLink.getPk()).build(),
                                                    "sk", AttributeValue.builder().s(accountTaskLink.getSk()).build()))
                                            .build()
                            )
                            .build()
            );

        });
        // create the transaction write request.
        TransactWriteItemsRequest transactWriteItemsRequest = TransactWriteItemsRequest.builder().transactItems(
                toBeDeleted
        ).build();
        transactionsRepository.transactionWrite(transactWriteItemsRequest);

    }

    private List<TaskAccountLink> fetchAndUpdateTaskAccountLinks(Task updatedTask) {
        /*
         * Helper method, it
         *   1) Fetches all task account links based on the updatedTask uuid.
         *   2) Updates the common attributes, between task and task account link.
         * */

        // fetch task account links from the database.
        List<TaskAccountLink> taskAccountLinks = taskAccountsService.getTaskAccounts(updatedTask.getTaskUuid());

        // update common attributes between task entity and TaskAccountLink entity.
        taskAccountLinks.forEach(
                (taskAccountLink) ->
                        taskAccountLink.setTaskTitle(updatedTask.getTitle()) // update taskTitle in TaskAccountLink.
        );
        return taskAccountLinks;
    }

    private List<AccountTaskLink> fetchAndUpdateAccountTaskLinks(List<TaskAccountLink> taskAccountLinks,
                                                                 Task updatedTask) {
        /*
         * Helper method, it
         *   1) Fetches all account task links based on task Account links, why ?
         *       as I need the account uuid.
         *   2) Updates the common attributes between account task link and the updated task.
         * */

        // fetch all AccountTaskLinks from the database.
        List<AccountTaskLink> accountTaskLinks = taskAccountLinks
                .stream()
                .map(
                        (taskAccountLink) ->
                                accountTasksService
                                        .getByAccountUuidAndTaskUuid(
                                                taskAccountLink.getAccountUuid(), updatedTask.getTaskUuid()
                                        )
                )
                .toList();

        // update common attributes between task entity and account task link entity.
        accountTaskLinks.forEach(
                accountTaskLink ->
                        accountTaskLink.setTaskTitle(updatedTask.getTitle()) // update task title in account task link.
        );
        return accountTaskLinks;
    }

    private List<TransactWriteItem> getTransactWriteItemsFromTaskAccountLinks(List<TaskAccountLink> taskAccountLinks) {
        List<Put> updateTaskAccountLinks = taskAccountLinks
                .stream()
                .map((taskAccountLink) -> Put
                        .builder()
                        .tableName("app")
                        .item(
                                taskAccountLinkTable.tableSchema().itemToMap(taskAccountLink, false)
                        )
                        .build()
                )
                .toList();
        return updateTaskAccountLinks.stream()
                .map(taskAccountLink ->
                        TransactWriteItem
                                .builder()
                                .put(taskAccountLink)
                                .build()
                )
                .toList();
    }

    private List<TransactWriteItem> getTransactWriteItemsFromAccountTaskLinks(List<AccountTaskLink> accountTaskLinks) {
        List<Put> updateTaskAccountLinks = accountTaskLinks
                .stream()
                .map((accountTaskLink) -> Put
                        .builder()
                        .tableName("app")
                        .item(
                                accountTaskLinkTable.tableSchema().itemToMap(accountTaskLink, false)
                        )
                        .build()
                )
                .toList();
        return updateTaskAccountLinks.stream()
                .map(taskAccountLink ->
                        TransactWriteItem
                                .builder()
                                .put(taskAccountLink)
                                .build()
                )
                .toList();
    }
}
