package com.example.project6.entity;


import com.example.project6.Enum.EntityType;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.util.UUID;
@DynamoDbBean
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

    @DynamoDbPartitionKey
    public String getPk() {
        return String.format("%s#%s", EntityType.ACCOUNT, accountUuid) ;
    }

    @DynamoDbSortKey
    public String getSk() {
        return String.format("%s#%s", EntityType.TASK, taskUuid == null ? "" : taskUuid) ;
    }

    @DynamoDbAttribute(value = "account_name")
    public String getAccountName() {
        return accountName;
    }

    @DynamoDbAttribute(value = "account_uuid")
    public UUID getAccountUuid() {
        return accountUuid;
    }

    @DynamoDbAttribute(value = "task_title")
    public String getTaskTitle() {
        return taskTitle;
    }

    @DynamoDbAttribute(value = "task_uuid")
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
