package com.example.project6.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.example.project6.entity.Account;
import org.springframework.stereotype.Repository;

@Repository
public class AccountRepository {
    final private  DynamoDBMapper dynamoDBMapper ;

    public AccountRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public String save(Account account){
        dynamoDBMapper.save(account);
        return account.getEmail();
    }

    public Account getAccountByPk(String pk) {
        return dynamoDBMapper.load(Account.class, pk);
    }

    public void deleteAccountByPk(String pk) {
        Account account = getAccountByPk(pk);
        dynamoDBMapper.delete(account);
    }
}
