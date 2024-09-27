package com.example.project6.Service;

import com.example.project6.Enum.EntityType;
import com.example.project6.dao.DBItemRepository;


import com.example.project6.entity.DBItem;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TaskService {

    private final DBItemRepository dbItemRepository;

    public TaskService(DBItemRepository dbItemRepository) {
        this.dbItemRepository = dbItemRepository;
    }

    public DBItem save(DBItem item){
        if(item.getTaskUuid() == null){
            item.setTaskUuid(UUID.randomUUID());
        }

        // Save the task entity.
        item.setPk(String.format("TASK#%s", item.getTaskUuid()));
        item.setSk(String.format("TASK#%s", item.getTaskUuid()));
        item.setEntityType(EntityType.TASK);

        return dbItemRepository.save(item);

    }

    public void addTaskToAccount(UUID accountUuid, DBItem item){
        // fetch the account from the database.
        DBItem dbAccount = dbItemRepository
                .load(String.format("ACCOUNT#%s", accountUuid), String.format("ACCOUNT#%s", accountUuid));

        // create task uuid if it isn't created.
        if(item.getTaskUuid() == null){
            item.setTaskUuid(UUID.randomUUID());
        }

        UUID taskUuid = item.getTaskUuid();

        // Save the task entity.
        item.setPk(String.format("TASK#%s", taskUuid));
        item.setSk(String.format("TASK#%s", taskUuid));
        item.setEntityType(EntityType.TASK);
        dbItemRepository.save(item);

        // create Account_Task_Link
        DBItem accountTaskLink = DBItem.builder()
                .withPk(String.format("ACCOUNT#%s", accountUuid))
                .withSk(String.format("TASK#%s", taskUuid))
                .withAccountUuid(accountUuid)
                .withTaskUuid(taskUuid)
                .withTitle(item.getTitle())
                .withEntityType(EntityType.ACCOUNT_TASK_LINK)
                .build();
        dbItemRepository.save(accountTaskLink);

        // create Task_Account_Link
        DBItem taskAccountLink = DBItem.builder()
                .withPk(String.format("TASK#%s", taskUuid))
                .withSk(String.format("ACCOUNT#%s", accountUuid))
                .withAccountUuid(accountUuid)
                .withTaskUuid(taskUuid)
                .withTitle(dbAccount.getName())
                .withEntityType(EntityType.TASK_ACCOUNT_LINK)
                .build();
        dbItemRepository.save(taskAccountLink);

    }
}
