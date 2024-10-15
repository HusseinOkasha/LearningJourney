package com.example.project6.dao;



import com.example.project6.entity.AccountTaskLink;
import com.example.project6.entity.TaskAccountLink;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
}
