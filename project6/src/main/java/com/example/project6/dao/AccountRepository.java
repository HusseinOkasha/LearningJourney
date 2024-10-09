package com.example.project6.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.example.project6.Enum.Role;
import com.example.project6.entity.Account;


import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


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
                .withIndexName("ROLE_INDEX") // Specify the GSI name
                .withConsistentRead(false) // GSI queries cannot be strongly consistent
                .withExpressionAttributeNames(Map.of("#r", "role" ))
                .withExpressionAttributeValues(Map.of(":roleValue", new AttributeValue().withS(role.toString()))) // Provide the value for gsi_pk
                .withKeyConditionExpression("#r = :roleValue"); // Define the condition for the GSI partition key

        return  dynamoDBMapper.query(Account.class, queryExpression);
    }

    public Optional<Account> findByEmail(String email){
        DynamoDBQueryExpression<Account> queryExpression = new DynamoDBQueryExpression<Account>()
                .withIndexName("EMAIL_INDEX") // Specify the GSI name
                .withConsistentRead(false) // GSI queries cannot be strongly consistent
                .withKeyConditionExpression("email = :gsiPkValue") // Define the condition for the GSI partition key
                .withExpressionAttributeValues(Map.of(":gsiPkValue", new AttributeValue().withS(email))); // Provide the value for gsi_pk
        List<Account> result = dynamoDBMapper.query(Account.class, queryExpression);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public void deleteByAccountUuid(Account account) {
        dynamoDBMapper.delete(account);
    }
}