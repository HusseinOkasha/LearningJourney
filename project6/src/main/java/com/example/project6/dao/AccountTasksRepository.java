package com.example.project6.dao;





import com.example.project6.entity.AccountTaskLink;
import com.example.project6.entity.Task;
import org.springframework.stereotype.Repository;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

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

    public Optional<AccountTaskLink> getByAccountUuidAndTaskUuid(UUID accountUuid, UUID taskUuid){
        AccountTaskLink accountTaskLink = AccountTaskLink
                .builder()
                .withAccountUuid(accountUuid)
                .withTaskUuid(taskUuid)
                .build();

        return Optional.ofNullable(accountTaskLinkTable.getItem(accountTaskLink));
    }

    public Optional<AccountTaskLink> load(AccountTaskLink accountTaskLink){

        return Optional.ofNullable(accountTaskLinkTable.getItem(accountTaskLink));
    }


    public Put generatePutAction(AccountTaskLink accountTaskLink){
        // Takes AccountTaskLink parameter and generates a Put action for it.
        return Put
                .builder()
                .tableName("app")
                .item(
                        accountTaskLinkTable.tableSchema().itemToMap(accountTaskLink, false)
                )
                .build();
    }
    public Delete generateDeleteAction(AccountTaskLink accountTaskLink){
        // takes Account task link parameter and generated a delete action for it.

        // create a map containing the partition key and sort key.
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("pk", AttributeValue.builder().s(accountTaskLink.getPk()).build());
        key.put("sk", AttributeValue.builder().s(accountTaskLink.getSk()).build());

        // create the delete action for the account task link.
        return Delete.builder().tableName("app").key(key).build();
    }

    public TransactWriteItem generatePutTransactWriteItem(AccountTaskLink accountTaskLink){
        // It takes Account task link entity as a parameter.
        // Generates put action for the provided accountTaskLink.
        // Generates a TransactWriteItem from the generated put action.

        // generates put action for the provided account task link.
        Put putAction = generatePutAction(accountTaskLink);

        // generates TransactWriteItem from the generated put action.
        return TransactWriteItem
                .builder()
                .put(putAction)
                .build();
    }
    public TransactWriteItem generateDeleteTransactWriteItem(AccountTaskLink accountTaskLink){
        // It takes Account task link entity as a parameter.
        // Generates delete action for the provided accountTaskLink.
        // Generates a TransactWriteItem from the generated delete action.

        // generates put action for the provided account task link.
        Delete deleteAction = generateDeleteAction(accountTaskLink);
        return TransactWriteItem.builder().delete(deleteAction).build();

    }



}
