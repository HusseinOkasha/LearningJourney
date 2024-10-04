package com.example.project6.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.example.project6.Enum.EntityType;

import java.util.UUID;
@DynamoDBTable(tableName = "app")
public class AccountTaskLink {


    // Composite Primary Key
    private String pk; // partition key
    private String sk; // sort key

    // **** Account attributes ****
    private String accountName;
    private UUID accountUuid;

    // **** task attributes ****
    private String taskTitle;
    private UUID taskUuid;

    // **** Constructors ****
    public AccountTaskLink(){
    }

    private AccountTaskLink(Builder builder){
        // **** Composite primary key ****
        this.pk = builder.pk;
        this.sk = builder.sk;

        // **** Account Attributes ****
        this.accountName = builder.accountName;
        this.accountUuid = builder.accountUuid;

        // **** Task Attributes ****
        this.taskTitle = builder.taskTitle;
        this.taskUuid = builder.taskUuid;

    }

    // **** methods ****
    public static Builder builder() {
        // creates and returns an instance of Builder.
        return new Builder();
    }

    @DynamoDBHashKey
    public String getPk() {
        return String.format("%s#%s", EntityType.ACCOUNT, accountUuid) ;
    }

    @DynamoDBRangeKey
    public String getSk() {
        return String.format("%s#%s", EntityType.TASK, taskUuid == null ? "" : taskUuid) ;
    }

    @DynamoDBAttribute(attributeName = "account_name")
    public String getAccountName() {
        return accountName;
    }

    @DynamoDBAttribute(attributeName = "account_uuid")
    public UUID getAccountUuid() {
        return accountUuid;
    }

    @DynamoDBAttribute(attributeName = "task_title")
    public String getTaskTitle() {
        return taskTitle;
    }

    @DynamoDBAttribute(attributeName = "task_uuid")
    public UUID getTaskUuid() {
        return taskUuid;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setAccountUuid(UUID accountUuid) {
        this.accountUuid = accountUuid;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public void setTaskUuid(UUID taskUuid) {
        this.taskUuid = taskUuid;
    }

    public static class Builder {


        // **** Composite Key ****
        private String pk;
        private String sk;

        // **** Account Attribute ****
        public String accountName;
        private UUID accountUuid;

        // **** Task Attributes ****
        public String taskTitle;
        public UUID taskUuid;

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
        public Builder withAccountUuid(UUID uuid){
            this.accountUuid = uuid;
            return this;
        }
        public Builder withAccountName(String accountName){
            this.accountName = accountName;
            return this;
        }

        // **** Account Task Link ****
        public Builder withTaskUuid(UUID taskUuid){
            this.taskUuid = taskUuid;
            return this;
        }
        public Builder withTaskTitle(String taskTitle){
            this.taskTitle = taskTitle;
            return this;
        }

        public AccountTaskLink build() {
            return new AccountTaskLink(this);
        }
    }



}
