package com.example.project6.dao;

import com.example.project6.entity.Task;
import com.example.project6.entity.TaskAccountLink;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.Delete;
import software.amazon.awssdk.services.dynamodb.model.Put;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;

import java.util.HashMap;
import java.util.Map;
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
    public Put generatePutAction(Task task){
        // Takes task parameter and generates a Put action for it.
        return Put
                .builder()
                .tableName("app")
                .item(
                        taskTable.tableSchema().itemToMap(task, false)// false: won't map null values
                )
                .build();
    }
    public Delete generateDeleteAction(Task task){
        // takes task parameter and generated a delete action for it.

        // create a map containing the partition key and sort key.
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("pk", AttributeValue.builder().s(task.getPk()).build());
        key.put("sk", AttributeValue.builder().s(task.getSk()).build());

        // create the delete action for the task.
        return Delete.builder().tableName("app").key(key).build();
    }
    public TransactWriteItem generatePutTransactWriteItem(Task task){
        // It takes task entity as a parameter.
        // Generates put action for the provided taskAccountLink.
        // Generates a TransactWriteItem from the generated put action.

        // generates put action for the provided account task link.
        Put putAction = generatePutAction(task);

        // generates TransactWriteItem from the generated put action.
        return TransactWriteItem
                .builder()
                .put(putAction)
                .build();
    }
    public TransactWriteItem generateDeleteTransactWriteItem(Task task){
        // It takes Task entity as a parameter.
        // Generates delete action for the provided Task.
        // Generates a TransactWriteItem from the generated delete action.

        // generates put action for the provided task account link.
        Delete deleteAction = generateDeleteAction(task);
        return TransactWriteItem.builder().delete(deleteAction).build();

    }
}
