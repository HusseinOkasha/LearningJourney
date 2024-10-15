package com.example.project6.dao;

import com.example.project6.entity.Task;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

import java.util.Optional;

@Repository
public class TaskRepository {
    final private DynamoDbTable<Task> taskTable;

    public TaskRepository(DynamoDbTable<Task> taskTable) {
        this.taskTable = taskTable;
    }
    // TODO: change return type to void.
    public Task save(Task task){
        taskTable.putItem(task);
        return task;
    }
    public Optional<Task> load(Task task){
        return Optional.ofNullable(taskTable.getItem(task));

    }
}
