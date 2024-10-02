package com.example.project6.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.example.project6.entity.AccountTaskLink;
import org.springframework.stereotype.Repository;

@Repository
public class AccountTaskRepository {
    private final DynamoDBMapper dynamoDBMapper;

    public AccountTaskRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }
    public void save(AccountTaskLink accountTaskLink){
        this.dynamoDBMapper.save(accountTaskLink);
    }

}
