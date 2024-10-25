package com.example.project6.Service;


import com.example.project6.Enum.Role;
import com.example.project6.Enum.TaskStatus;
import com.example.project6.dao.TaskRepository;
import com.example.project6.dao.TransactionsRepository;
import com.example.project6.entity.Account;
import com.example.project6.entity.AccountTaskLink;
import com.example.project6.entity.Task;
import com.example.project6.entity.TaskAccountLink;
import com.example.project6.exception.NotFoundException;
import com.example.project6.security.CustomUserDetails;
import org.aspectj.weaver.ast.Not;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.*;


import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskAccountsService taskAccountsService;

    private final AccountService accountService;
    private final AuthenticationService authenticationService;
    private final AccountTasksService accountTasksService;
    private final TransactionsRepository transactionsRepository;

    public TaskService(TaskRepository taskRepository,
                       AccountService accountService, AuthenticationService authenticationService,
                       TaskAccountsService taskAccountsService,
                       TransactionsRepository transactionsRepository,
                       AccountTasksService accountTasksService) {

        this.taskRepository = taskRepository;
        this.taskAccountsService = taskAccountsService;
        this.accountService = accountService;
        this.authenticationService = authenticationService;
        this.accountTasksService = accountTasksService;
        this.transactionsRepository = transactionsRepository;
    }

    public Task createNewTask(Task task) {
        /*
         * creates new task, accountTaskLink, and taskAccountLink
         * All three are saved to the database atomically ( inside transaction).
         * */

        // get the currently authenticated account.
        Account currentlyAuthenticatedAccount = authenticationService.getAuthenticatedAccount();

        // create task uuid if it isn't created.
        generateUuid(task);

        // build accountTaskLink.
        AccountTaskLink accountTaskLink = buildAccountTaskLinkWith(currentlyAuthenticatedAccount, task);

        // build taskAccountLink
        TaskAccountLink taskAccountLink = buildTaskAccountLinkWith(currentlyAuthenticatedAccount, task);

        // build TransactWriteItems for (task, accountTaskLink, and taskAccountLink)
        // with PUT action.
        List<TransactWriteItem> transactWriteItems = buildPutTransactWriteItemsWith(
                Stream.of(accountTaskLink), Stream.of(taskAccountLink), task
        );

        // create write transaction request.
        TransactWriteItemsRequest transactWriteItemsRequest = TransactWriteItemsRequest.builder().transactItems(
                transactWriteItems
        ).build();

        // perform the transaction on the database.
        transactionsRepository.transactionWrite(transactWriteItemsRequest);

        return taskRepository.load(task).orElseThrow(
                ()->new NotFoundException("couldn't find task with uuid: " + task.getTaskUuid())
        );
    }

    public Task getTaskByUuid(UUID taskUuid) {
        // check if the currently authenticated account has the authority to get the task with the given taskUuid.

        Task task = Task.builder().withTaskUuid(taskUuid).build();
        return taskRepository
                .load(task)
                .orElseThrow( // throws exception in case the task isn't found.
                        () -> new NotFoundException("There is no task with uuid: " + taskUuid)
                );
    }

    public Task updateTaskByTaskUuid(Task task, UUID taskUuid) {
        /*
         * It tries to update all task attributes.
         *   1) Fetches the task to be updated from the database, set its attributes to the updated values.
         *   2) Fetches task account links from the database,
         *       then update common attributes between task and task account link.
         *   3) Fetches account task links from the database,
         *       then update common attributes between task and account task link.
         *   4) Creates a write transaction to save the updates in Task, TaskAccountLinks and AccountTaskLinks
         *       to the database.
         *
         * */

        // check if the currently authenticated account has the authority to update the task with the given taskUuid.


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
        TransactWriteItem taskPutTransactWriteItem = generatePutTransactWriteItem(dbTask);

        // create transact write items for account task links and task account links.
        List<TransactWriteItem> transactWriteItems = Stream
                .concat(
                        taskAccountLinks.stream().map(taskAccountsService::generatePutTransactWriteItem),
                        accountTaskLinks.stream().map(accountTasksService::generatePutTransactWriteItem))
                .collect(Collectors.toList());

        // add task transact write to transact write items list.
        transactWriteItems.add(taskPutTransactWriteItem);

        // create the transaction write request.
        TransactWriteItemsRequest transactWriteItemsRequest = TransactWriteItemsRequest.builder().transactItems(
                transactWriteItems
        ).build();

        // fire the transaction to the database
        transactionsRepository.transactionWrite(transactWriteItemsRequest);

        // fetch the task after update from the database.
        return getTaskByUuid(taskUuid);
    }

    public Task updateTaskTitleByUuid(String title, UUID taskUuid) {
        /*
         * It tries to update task title by uuid.
         *   1) Fetches the task to be updated from the database, set the title to the updated title,
         *   2) Fetches task account links from the database,
         *       then updates taskTitle.
         *   3) Fetches account task links from the database,
         *       then updates the task title.
         *   4) Creates a write transaction to save the updates in Task, TaskAccountLinks,
         *       and AccountTaskLinks to the database.
         * */

        // check if the currently authenticated account has the authority to update the task with the given taskUuid.


        // fetch the task from the database.
        Task dbTask = this.getTaskByUuid(taskUuid);

        // update the dbTask.
        dbTask.setTitle(title);

        // Fetch and Update (task account link) & (account task link).
        List<TaskAccountLink> taskAccountLinks = fetchAndUpdateTaskAccountLinks(dbTask);
        List<AccountTaskLink> accountTaskLinks = fetchAndUpdateAccountTaskLinks(taskAccountLinks, dbTask);

        // create task transact item.
        TransactWriteItem taskPutTransactWriteItem = generatePutTransactWriteItem(dbTask);

        // create transact write items for account task links and task account links.
        List<TransactWriteItem> transactWriteItems = Stream
                .concat(
                        taskAccountLinks.stream().map(taskAccountsService::generatePutTransactWriteItem),
                        accountTaskLinks.stream().map(accountTasksService::generatePutTransactWriteItem))
                .collect(Collectors.toList());

        // add task transact write to transact write items list.
        transactWriteItems.add(taskPutTransactWriteItem);

        // create the transaction write request.
        TransactWriteItemsRequest transactWriteItemsRequest = TransactWriteItemsRequest.builder().transactItems(
                transactWriteItems
        ).build();

        // fire the transaction to the database
        transactionsRepository.transactionWrite(transactWriteItemsRequest);

        // fetch the task after update from the database.
        return getTaskByUuid(taskUuid);
    }

    public Task updateTaskDescriptionByUuid(String description, UUID taskUuid) {
        /*
         * Update the task description using the task uuid.
         *   1) Fetches the task we want to update.
         *   2) Sets the tasks description with the new description.
         *   3) Save the task after performing the update to the database.
         * */
        // check if the currently authenticated account has the authority to update the task with the given taskUuid.


        // fetch the task from the database.
        Task dbTask = this.getTaskByUuid(taskUuid);

        // update the db task description.
        dbTask.setDescription(description);

        // save the task to the database.
        this.taskRepository.save(dbTask);

        // fetch the task after update from the database.
        return getTaskByUuid(taskUuid);

    }

    public Task updateTaskStatusByUuid(TaskStatus status, UUID taskUuid) {
        /*
         * Updates the task status using task uuid.
         *   1) Fetches the task we want to update.
         *   2) Sets the task status with the new status.
         *   3) Save the task after performing the update to the database.
         * */

        // check if the currently authenticated account has the authority to update the task with the given taskUuid.


        // fetch the task from the database.
        Task dbTask = this.getTaskByUuid(taskUuid);

        // update the dbTask status.
        dbTask.setStatus(status);

        // save the task to the database.
        this.taskRepository.save(dbTask);

        // fetch the task after update from the database.
        return getTaskByUuid(taskUuid);
    }

    public void deleteTaskByUuid(UUID taskUuid) {
        // task to be deleted.
        Task task = Task
                .builder()
                .withTaskUuid(taskUuid)
                .build();

        // fetch all task account links from database.
        List<TaskAccountLink> taskAccountLinks = taskAccountsService.getTaskAccounts(taskUuid);

        // fetch all account task links from the database.
        List<AccountTaskLink> accountTaskLinks = taskAccountLinks.stream().map(taskAccountLink ->
                AccountTaskLink.builder()
                        .withTaskUuid(taskAccountLink.getTaskUuid())
                        .withAccountUuid(taskAccountLink.getAccountUuid())
                        .build()
        ).toList();

        // create transact write items for ( task account links & account task links).
        List<TransactWriteItem> transactWriteItems = Stream.concat(
                        taskAccountLinks.stream().map(taskAccountsService::generateDeleteTransactWriteItem),
                        accountTaskLinks.stream().map(accountTasksService::generateDeleteTransactWriteItem)
                )
                .collect(Collectors.toList());

        // create transact write item for (task)
        transactWriteItems.add(generateDeleteTransactWriteItem(task));

        // create the transaction write request.
        TransactWriteItemsRequest transactWriteItemsRequest = TransactWriteItemsRequest.builder().transactItems(
                transactWriteItems
        ).build();

        // fire the transaction.
        transactionsRepository.transactionWrite(transactWriteItemsRequest);

    }
    public Task save(Task task){
        generateUuid(task);
        taskRepository.save(task);
        return task;
    }
    public TransactWriteItem generatePutTransactWriteItem(Task task) {
        return taskRepository.generatePutTransactWriteItem(task);
    }

    public TransactWriteItem generateDeleteTransactWriteItem(Task task) {
        return taskRepository.generateDeleteTransactWriteItem(task);
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

    public boolean isTaskSharedWithUser(UUID taskUuid, CustomUserDetails customUserDetails) {

        UUID accountUuid = customUserDetails.getAccountUuid();
        AccountTaskLink accountTaskLink = accountTasksService.getByAccountUuidAndTaskUuid(accountUuid, taskUuid);
        return true;
    }

    private void generateUuid(Task task) {
        /*
         * generates uuid for the task if it is not created yet
         * */
        if (task.getTaskUuid() == null) {
            task.setTaskUuid(UUID.randomUUID());
        }

    }

    private List<TransactWriteItem> buildPutTransactWriteItemsWith(
            Stream<AccountTaskLink> accountTaskLinks,
            Stream<TaskAccountLink> taskAccountLinks,
            Task task) {
        /*
         * It's a helper method builds transactWrites with Put action from the provided:
         *   - accountTaskLinks.
         *   - taskAccountLinks.
         *   - task.
         * */

        List<TransactWriteItem> transactWriteItems;

        transactWriteItems = Stream.concat(
                // generate put transact write items for accountTaskLinks
                accountTaskLinks.map(accountTasksService::generatePutTransactWriteItem),

                // generate put transact write items for taskAccountLinks.
                taskAccountLinks.map(taskAccountsService::generatePutTransactWriteItem)
        ).collect(Collectors.toList());

        // generate put transact write item for task.
        transactWriteItems.add(generatePutTransactWriteItem(task));

        // returns the transact write items list, which contains transact write items for
        // accountTaskLinks, taskAccountLinks, and task. With PUT action.
        return transactWriteItems;
    }

    private List<TransactWriteItem> buildDeleteTransactWriteItemsWith(
            Stream<AccountTaskLink> accountTaskLinks,
            Stream<TaskAccountLink> taskAccountLinks,
            Task task) {
        /*
         * It's a helper method builds transactWrites with DELETE action from the provided:
         *   - accountTaskLinks.
         *   - taskAccountLinks.
         *   - task.
         * */

        // in case the action is delete.
        List<TransactWriteItem> transactWriteItems = Stream.concat(
                // generate delete transact write items for accountTaskLinks.
                accountTaskLinks.map(accountTasksService::generateDeleteTransactWriteItem),
                // generate delete transact write items for taskAccountLinks.
                taskAccountLinks.map(taskAccountsService::generateDeleteTransactWriteItem)
        ).collect(Collectors.toList());

        // generate write transact item for the task.
        transactWriteItems.add(generateDeleteTransactWriteItem(task));

        // returns the transact write items list, which contains transact write items for
        // accountTaskLinks, taskAccountLinks, and task. With DELETE action.
        return transactWriteItems;
    }

    private TaskAccountLink buildTaskAccountLinkWith(Account account, Task task) {
        /*
         * It's a helper method that builds taskAccountLink entity.
         * Arguments:
         *   - Account: is the account that will be linked to the task.
         *   - Task: is the task which the account will be linked to.
         * It returns TaskAccountLink based on the provided account and task.
         * */
        return TaskAccountLink.builder()
                .withAccountUuid(account.getAccountUuid())
                .withAccountName(account.getName())
                .withTaskUuid(task.getTaskUuid())
                .withTaskTitle(task.getTitle())
                .build();
    }

    private AccountTaskLink buildAccountTaskLinkWith(Account account, Task task) {
        /*
         * It's a helper method that builds accountTaskLink entity.
         * Arguments:
         *   - Account: is the account that will be linked to the task.
         *   - Task: is the task which the account will be linked to.
         * It returns an accountTaskLink based on the provided account and task.
         * */

        return AccountTaskLink.builder()
                .withAccountUuid(account.getAccountUuid())
                .withAccountName(account.getName())
                .withTaskUuid(task.getTaskUuid())
                .withTaskTitle(task.getTitle())
                .build();
    }

}
