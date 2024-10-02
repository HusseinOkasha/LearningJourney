package com.example.project6.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.example.project6.entity.TaskAccountLink;
import org.springframework.stereotype.Repository;

@Repository
public class TaskAccountLinkRepository {
    private final DynamoDBMapper dynamoDBMapper;

    public TaskAccountLinkRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public void save(TaskAccountLink taskAccountLink){
        this.dynamoDBMapper.save(taskAccountLink);
    }
}
