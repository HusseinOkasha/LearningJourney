package com.example.project6.entity;


import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.example.project6.Enum.EntityType;
import com.example.project6.Enum.Role;
import com.example.project6.util.entityAndDtoMappers.UUIDConverter;

import java.util.UUID;

@DynamoDBTable(tableName = "app")
public class Account {

    // Composite Primary Key
    private String pk; // partition key
    private String sk; // sort key

    // **** Account attributes ****
    private UUID accountUuid;
    private String name;
    private String email;
    private String password;
    private Role role;

    // **** Constructors ****
    public Account(){}

    private Account(Builder builder){
        // **** Composite primary key ****
        this.pk = builder.pk;
        this.sk = builder.sk;

        // **** Account Attributes ****
        this.accountUuid = builder.accountUuid;
        this.name = builder.name;
        this.email = builder.email;
        this.password = builder.password;
        this.role = builder.role;

    }

    // **** methods ****
    public static Builder builder() {
        // creates and returns an instance of Builder.
        return new Builder();
    }

    @DynamoDBHashKey(attributeName = "pk")
    public String getPk() {
        return String.format("%s#%s", EntityType.ACCOUNT, accountUuid) ;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    @DynamoDBRangeKey(attributeName = "sk")
    public String getSk() {
        return String.format("%s#%s", EntityType.ACCOUNT, accountUuid) ;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    @DynamoDBAttribute(attributeName = "account_uuid")
    @DynamoDBTypeConverted(converter = UUIDConverter.class)
    public UUID getAccountUuid() {
        return accountUuid;
    }

    public void setAccountUuid(UUID accountUuid) {
        this.accountUuid = accountUuid;
    }

    @DynamoDBAttribute(attributeName = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    @DynamoDBAttribute(attributeName = "email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @DynamoDBAttribute(attributeName = "password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @DynamoDBTypeConvertedEnum
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "GSI1", attributeName = "GSI1PK")
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void generateAccountUuid(){
        if (accountUuid == null) {
            this.accountUuid = UUID.randomUUID();
        }
    }

    public static class Builder {
        // **** Composite Key ****
        private String pk;
        private String sk;

        // **** Account Attribute ****
        private String name ;
        private String email;
        private String password;
        private Role role;
        private UUID accountUuid;

        public Builder() {
        }

        // **** Composite Key ****
        public Builder withPk(String pk){
            this.pk = pk;
            return this;
        }
        public Builder withSk(String sk){
            this.sk = sk;
            return this;
        }

        // **** Account Attributes ****
        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }
        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }
        public Builder withName(String name) {
            this.name = name;
            return this;
        }
        public Builder withRole(Role role){
            this.role = role;
            return this;
        }
        public Builder withAccountUuid(UUID uuid){
            this.accountUuid = uuid;
            return this;
        }

        public Account build() {
            return new Account(this);
        }
    }

}