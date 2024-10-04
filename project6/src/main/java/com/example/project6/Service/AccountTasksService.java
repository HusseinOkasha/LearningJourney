package com.example.project6.Service;

import com.example.project6.dao.AccountTaskRepository;
import com.example.project6.entity.AccountTaskLink;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AccountTasksService {
    private final AccountTaskRepository accountTaskRepository;

    public AccountTasksService(AccountTaskRepository accountTaskRepository) {
        this.accountTaskRepository = accountTaskRepository;
    }

    public List<AccountTaskLink> getAccountTasks(UUID accountUuid) {
        return accountTaskRepository.getAccountTasks(accountUuid);
    }
}
