package com.example.project6.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import com.example.project6.entity.DBItem;
import org.springframework.stereotype.Repository;

@Repository
public class DBItemRepository {
    final private DynamoDBMapper dynamoDBMapper ;


    public DBItemRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public DBItem save(DBItem item){
        dynamoDBMapper.save(item);
        return item;
    }
    public DBItem load(String pk, String sk){
        return  dynamoDBMapper.load( DBItem.class , pk, sk);

    }

//    public Account getByUuid(String uuid) {
//        String pk = String.format("ACCOUNT#%s", uuid);
//        String sk = String.format("ACCOUNT#%s", uuid);
//
//        return dynamoDBMapper.load(Account.class, pk, sk);
//    }
//
//    public void deleteByPk(String pk) {
//        Account account = getByUuid(pk);
//        dynamoDBMapper.delete(account);
//    }
}
