package com.example.project6.Service;


import com.example.project6.dao.AccountTaskRepository;
import com.example.project6.dao.TaskAccountLinkRepository;
import com.example.project6.dao.TaskRepository;
import com.example.project6.entity.Account;
import com.example.project6.entity.AccountTaskLink;
import com.example.project6.entity.Task;
import com.example.project6.entity.TaskAccountLink;
import org.springframework.stereotype.Service;


import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final AccountService accountService;
    private final AccountTaskRepository accountTaskRepository;
    private final TaskAccountLinkRepository taskAccountLinkRepository;


    public TaskService(TaskRepository taskRepository, AccountService accountService, AccountTaskRepository accountTaskRepository, TaskAccountLinkRepository taskAccountLinkRepository) {
        this.taskRepository = taskRepository;
        this.accountService = accountService;
        this.accountTaskRepository = accountTaskRepository;
        this.taskAccountLinkRepository = taskAccountLinkRepository;
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

        // Save the task entity
        taskRepository.save(task);

        // create Account_Task_Link
        AccountTaskLink accountTaskLink = AccountTaskLink.builder()
                .withAccountUuid(accountUuid)
                .withTaskUuid(taskUuid)
                .withAccountName(dbAccount.getName())
                .withTaskTitle(task.getTitle())
                .build();

        // save Account_Task_Link to the database.
        accountTaskRepository.save(accountTaskLink);

        // create Task_Account_Link
        TaskAccountLink taskAccountLink = TaskAccountLink.builder()
                .withPk(String.format("TASK#%s", taskUuid))
                .withSk(String.format("ACCOUNT#%s", accountUuid))
                .withAccountUuid(accountUuid)
                .withTaskUuid(taskUuid)
                .withTaskTitle(dbAccount.getName())
                .build();

        // save Task_Account_Link to the database.
        taskAccountLinkRepository.save(taskAccountLink);
    }
}
