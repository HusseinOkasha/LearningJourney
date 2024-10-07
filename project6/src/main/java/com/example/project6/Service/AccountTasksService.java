package com.example.project6.Service;

import com.example.project6.dao.AccountTasksRepository;
import com.example.project6.entity.AccountTaskLink;
import org.springframework.stereotype.Service;

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
        return accountTasksRepository.getByAccountUuidAndTaskUuid(accountUuid, taskUuid);
    }
}
