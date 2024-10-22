package com.example.project6.Service;

import com.example.project6.dao.AccountTasksRepository;
import com.example.project6.entity.AccountTaskLink;
import com.example.project6.entity.Task;
import com.example.project6.exception.NotFoundException;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;

import java.util.List;
import java.util.UUID;

@Service
public class AccountTasksService {
    private final AccountTasksRepository accountTasksRepository;

    public AccountTasksService(AccountTasksRepository accountTasksRepository) {
        this.accountTasksRepository = accountTasksRepository;
    }

    public List<AccountTaskLink> getAccountTasks(UUID accountUuid) {
        return accountTasksRepository.getAccountTasks(accountUuid);
    }

    public AccountTaskLink getByAccountUuidAndTaskUuid(UUID accountUuid, UUID taskUuid){
        return accountTasksRepository.getByAccountUuidAndTaskUuid(accountUuid, taskUuid)
                .orElseThrow(()->new NotFoundException(
                        String.format("couldn't find task with uuid: %s that belongs to account with uuid: %s",
                                taskUuid, accountUuid)
                        )
                );
    }
    public TransactWriteItem generatePutTransactWriteItem(AccountTaskLink accountTaskLink){
        return accountTasksRepository.generatePutTransactWriteItem(accountTaskLink);
    }
    public TransactWriteItem generateDeleteTransactWriteItem(AccountTaskLink accountTaskLink){
        return accountTasksRepository.generateDeleteTransactWriteItem(accountTaskLink);
    }
}
