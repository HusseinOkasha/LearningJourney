package com.example.project6.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.TransactionLoadRequest;
import com.amazonaws.services.dynamodbv2.datamodeling.TransactionWriteRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public class TransactionsRepository {

    private final DynamoDBMapper dynamoDBMapper;

    public TransactionsRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public void transactionWrite(TransactionWriteRequest transactionWriteRequest){
        dynamoDBMapper.transactionWrite(transactionWriteRequest);
    }
    public List<Object> transactionLoad(TransactionLoadRequest transactionLoadRequest){
        return dynamoDBMapper.transactionLoad(transactionLoadRequest);
    }
}
