package com.example.project6.dao;

import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;

@Repository
public class TransactionsRepository {

    private final DynamoDbClient dynamoDbClient;

    public TransactionsRepository(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public void transactionWrite(TransactWriteItemsRequest transactWriteItemsRequest){
        dynamoDbClient.transactWriteItems(transactWriteItemsRequest);
    }
}
