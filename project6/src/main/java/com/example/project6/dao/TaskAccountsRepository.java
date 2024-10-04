package com.example.project6.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.example.project6.entity.AccountTaskLink;
import com.example.project6.entity.TaskAccountLink;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class TaskAccountsRepository {
    private final DynamoDBMapper dynamoDBMapper;

    public TaskAccountsRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public void save(TaskAccountLink taskAccountLink){
        this.dynamoDBMapper.save(taskAccountLink);
    }

    public List<TaskAccountLink> getTaskAccounts(UUID taskUuid) {
        TaskAccountLink taskAccountLink = TaskAccountLink.builder().withTaskUuid(taskUuid).build();

        // Create the key condition expression for the partition key
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":pk", new AttributeValue().withS(taskAccountLink.getPk()));
        eav.put(":skPrefix", new AttributeValue().withS(taskAccountLink.getSk()));

        // Define the query expression
        DynamoDBQueryExpression<TaskAccountLink> queryExpression = new DynamoDBQueryExpression<TaskAccountLink>()
                .withKeyConditionExpression("pk = :pk and begins_with(sk, :skPrefix)")
                .withExpressionAttributeValues(eav);

        // Execute the query
        return dynamoDBMapper.query(TaskAccountLink.class, queryExpression);

    }
}
