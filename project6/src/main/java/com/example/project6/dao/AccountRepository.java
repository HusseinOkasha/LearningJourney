package com.example.project6.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.example.project6.entity.Account;


import org.springframework.stereotype.Repository;


@Repository
public class AccountRepository {
    final private DynamoDBMapper dynamoDBMapper;

    public AccountRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public void save(Account account) {
        dynamoDBMapper.save(account);

    }

    public Account load(Account account) {
        return dynamoDBMapper.load(account);

    }
}