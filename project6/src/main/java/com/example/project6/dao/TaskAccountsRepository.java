package com.example.project6.dao;



import com.example.project6.entity.AccountTaskLink;
import com.example.project6.entity.Task;
import com.example.project6.entity.TaskAccountLink;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

@Repository
public class TaskAccountsRepository {
    private final DynamoDbTable<TaskAccountLink> taskAccountLinkTable;
    private final DynamoDbClient dynamoDbClient;

    public TaskAccountsRepository(DynamoDbTable<TaskAccountLink> taskAccountLinkTable, DynamoDbClient dynamoDbClient) {
        this.taskAccountLinkTable = taskAccountLinkTable;
        this.dynamoDbClient = dynamoDbClient;
    }


    public void save(TaskAccountLink taskAccountLink){
        this.taskAccountLinkTable.putItem(taskAccountLink);
    }
    public Optional<TaskAccountLink> load(TaskAccountLink taskAccountLink){
        return Optional.ofNullable(this.taskAccountLinkTable.getItem(taskAccountLink));
    }
    public void delete(TaskAccountLink taskAccountLink){
        this.taskAccountLinkTable.deleteItem(taskAccountLink);
    }
    public List<TaskAccountLink> getTaskAccounts(UUID taskUuid) {
        TaskAccountLink accountTaskLink = TaskAccountLink
                .builder()
                .withTaskUuid(taskUuid)
                .build();

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":pk", AttributeValue.builder().s(accountTaskLink.getPk()).build());
        expressionAttributeValues.put(":sk", AttributeValue.builder().s(accountTaskLink.getSk()).build());

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName("app")
                .keyConditionExpression("pk = :pk AND begins_with(sk, :sk)")
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        // Execute the query
        return dynamoDbClient.query(queryRequest)
                .items()
                .stream()
                .map(item-> taskAccountLinkTable.tableSchema().mapToItem(item))
                .toList();
    }

    public Put generatePutAction(TaskAccountLink taskAccountLink){
        // Takes AccountTaskLink parameter and generates a Put action for it.
        return Put
                .builder()
                .tableName("app")
                .item(
                        taskAccountLinkTable.tableSchema().itemToMap(taskAccountLink, false)// false: won't map null values
                )
                .build();
    }
    public Delete generateDeleteAction(TaskAccountLink taskAccountLink){
        // takes Account task link parameter and generated a delete action for it.

        // create a map containing the partition key and sort key.
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("pk", AttributeValue.builder().s(taskAccountLink.getPk()).build());
        key.put("sk", AttributeValue.builder().s(taskAccountLink.getSk()).build());

        // create the delete action for the account task link.
        return Delete.builder().tableName("app").key(key).build();
    }
    public TransactWriteItem generatePutTransactWriteItem(TaskAccountLink taskAccountLink){
        // It takes Account task link entity as a parameter.
        // Generates put action for the provided taskAccountLink.
        // Generates a TransactWriteItem from the generated put action.

        // generates put action for the provided account task link.
        Put putAction = generatePutAction(taskAccountLink);

        // generates TransactWriteItem from the generated put action.
        return TransactWriteItem
                .builder()
                .put(putAction)
                .build();
    }
    public TransactWriteItem generateDeleteTransactWriteItem(TaskAccountLink taskAccountLink){
        // It takes Task Account link entity as a parameter.
        // Generates delete action for the provided TaskAccountLink.
        // Generates a TransactWriteItem from the generated delete action.

        // generates put action for the provided task account link.
        Delete deleteAction = generateDeleteAction(taskAccountLink);
        return TransactWriteItem.builder().delete(deleteAction).build();

    }
}
