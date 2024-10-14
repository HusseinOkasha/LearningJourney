package com.example.project6.dao;




import com.example.project6.Enum.Role;
import com.example.project6.entity.Account;


import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.*;

import java.util.List;
import java.util.Optional;


@Repository
public class AccountRepository {
    final private DynamoDbTable<Account> accountTable;

    public AccountRepository(DynamoDbTable<Account> accountTable) {
        this.accountTable = accountTable;
    }


    public void save(Account account) {
        accountTable.putItem(
                PutItemEnhancedRequest
                        .builder(Account.class)
                        .item(account)
                        .build()
        );

    }

    public Account load(Account account) {
        return accountTable.getItem(account);
    }

    public List<Account> getAllByRole(Role role){
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder()
                                .partitionValue(role.toString()) // Specify the partition key value for the GSI
                                .build()
                ))
                .build();

        return accountTable.index("ROLE_INDEX").query(request)
                .stream()
                .flatMap(page -> page.items().stream())
                .toList();
    }

    public Optional<Account> findByEmail(String email){
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder()
                                .partitionValue(email) // Specify the partition key value for the GSI
                                .build()
                ))
                .build();

        return accountTable.index("EMAIL_INDEX")
                .query(request)
                .stream()
                .findFirst()
                .flatMap(page -> page.items().stream().findFirst());
    }

    public void deleteByAccountUuid(Account account) {
       accountTable.deleteItem(account);
    }
}