package com.example.project6.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.example.project6.Enum.Role;
import com.example.project6.entity.Account;


import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


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

    public List<Account> getAllByRole(Role role){
        DynamoDBQueryExpression<Account> queryExpression = new DynamoDBQueryExpression<Account>()
                .withIndexName("GSI1") // Specify the GSI name
                .withConsistentRead(false) // GSI queries cannot be strongly consistent
                .withKeyConditionExpression("GSI1PK = :gsiPkValue") // Define the condition for the GSI partition key
                .withExpressionAttributeValues(Map.of(":gsiPkValue", new AttributeValue().withS(role.toString()))); // Provide the value for gsi_pk
        return  dynamoDBMapper.query(Account.class, queryExpression);
    }
}