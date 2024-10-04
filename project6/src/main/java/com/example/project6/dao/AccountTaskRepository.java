package com.example.project6.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.example.project6.entity.AccountTaskLink;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class AccountTaskRepository {
    private final DynamoDBMapper dynamoDBMapper;

    public AccountTaskRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public void save(AccountTaskLink accountTaskLink){
        this.dynamoDBMapper.save(accountTaskLink);
    }

    public List<AccountTaskLink> getAccountTasks(UUID accountUuid){
        AccountTaskLink accountTaskLink = AccountTaskLink.builder().withAccountUuid(accountUuid).build();

        // Create the key condition expression for the partition key
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":pk", new AttributeValue().withS(accountTaskLink.getPk()));
        eav.put(":skPrefix", new AttributeValue().withS(accountTaskLink.getSk()));

        // Define the query expression
        DynamoDBQueryExpression<AccountTaskLink> queryExpression = new DynamoDBQueryExpression<AccountTaskLink>()
                .withKeyConditionExpression("pk = :pk and begins_with(sk, :skPrefix)")
                .withExpressionAttributeValues(eav);

        // Execute the query
        return dynamoDBMapper.query(AccountTaskLink.class, queryExpression);

    }
}
