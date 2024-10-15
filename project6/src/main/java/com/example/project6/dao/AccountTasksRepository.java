package com.example.project6.dao;





import com.example.project6.entity.AccountTaskLink;
import org.springframework.stereotype.Repository;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class AccountTasksRepository {
    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbTable<AccountTaskLink>accountTaskLinkTable;

    public AccountTasksRepository(DynamoDbClient dynamoDbClient, DynamoDbTable<AccountTaskLink> accountTaskLinkTable) {
        this.dynamoDbClient = dynamoDbClient;
        this.accountTaskLinkTable = accountTaskLinkTable;
    }

    public void save(AccountTaskLink accountTaskLink){
        accountTaskLinkTable.putItem(accountTaskLink);
    }

    public List<AccountTaskLink> getAccountTasks(UUID accountUuid){
        AccountTaskLink accountTaskLink  =  AccountTaskLink
                .builder()
                .withAccountUuid(accountUuid)
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
                .map(item-> accountTaskLinkTable.tableSchema().mapToItem(item)).toList();


    }

    public AccountTaskLink getByAccountUuidAndTaskUuid(UUID accountUuid, UUID taskUuid){
        AccountTaskLink accountTaskLink = AccountTaskLink
                .builder()
                .withAccountUuid(accountUuid)
                .withTaskUuid(taskUuid)
                .build();

        return accountTaskLinkTable.getItem(accountTaskLink);
    }
}
