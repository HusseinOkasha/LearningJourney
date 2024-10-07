package com.example.project6.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.example.project6.entity.Account;
import com.example.project6.entity.Task;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TaskRepository {
    final private DynamoDBMapper dynamoDBMapper ;

    public TaskRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public Task save(Task task){
        dynamoDBMapper.save(task);
        return task;
    }
    public Optional<Task> load(Task task){
        return Optional.ofNullable(dynamoDBMapper.load(task));

    }
}
