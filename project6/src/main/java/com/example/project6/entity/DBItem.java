package com.example.project6.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.example.project6.Enum.EntityType;
import com.example.project6.Enum.Role;
import com.example.project6.Enum.TaskStatus;
import com.example.project6.util.entityAndDtoMappers.UUIDConverter;

import java.util.UUID;

@DynamoDBTable(tableName = "app")
public class DBItem {


    // Composite Primary Key
    @DynamoDBHashKey
    private String pk; // partition key
    @DynamoDBRangeKey
    private String sk; // sort key

    // Specifies the type of the entity we are creating.
    @DynamoDBTypeConvertedEnum
    private EntityType entityType;

    // **** Account attributes ****
    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = UUIDConverter.class)
    private UUID accountUuid;

    @DynamoDBAttribute
    private String name;

    @DynamoDBAttribute
    private String email;

    @DynamoDBAttribute
    private String password;

    @DynamoDBTypeConvertedEnum
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "GSI1", attributeName = "GSI1PK")
    private Role role;


    // **** Task Attributes ****
    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = UUIDConverter.class)
    private UUID taskUuid;

    @DynamoDBAttribute
    private String title;

    @DynamoDBAttribute
    private String description;

    @DynamoDBAttribute
    @DynamoDBTypeConvertedEnum
    private TaskStatus status;


    // **** Comment Attributes ****
    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = UUIDConverter.class)
    private UUID commentUuid;

    @DynamoDBAttribute
    private String body;

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = UUIDConverter.class)
    private UUID creatorAccountUuid;

    private DBItem(Builder builder){
        // **** Composite Primary Key ****
        this.pk = builder.pk;
        this.sk = builder.sk;

        // **** Entity type ****
        this.entityType = builder.entityType;

        // **** Account Attributes ****
        this.accountUuid = builder.accountUuid;
        this.name = builder.name;
        this.email = builder.email;
        this.password = builder.password;
        this.role = builder.role;

        // **** Task Attributes ****.
        this.taskUuid = builder.taskUuid;
        this.description = builder.description;
        this.title = builder.title;
        this.status = builder.status;

        // **** CommentAttributes ****
        this.commentUuid = builder.commentUuid;
        this.body = builder.body;
        this.creatorAccountUuid = builder.creatorAccountUuid;

    }

    public DBItem() {
    }

    public static Builder builder() {
        // creates and returns an instance of Builder.
        return new Builder();
    }

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public UUID getAccountUuid() {
        return accountUuid;
    }

    public void setAccountUuid(UUID accountUuid) {
        this.accountUuid = accountUuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UUID getTaskUuid() {
        return taskUuid;
    }

    public void setTaskUuid(UUID taskUuid) {
        this.taskUuid = taskUuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public UUID getCommentUuid() {
        return commentUuid;
    }

    public void setCommentUuid(UUID commentUuid) {
        this.commentUuid = commentUuid;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public UUID getCreatorAccountUuid() {
        return creatorAccountUuid;
    }

    public void setCreatorAccountUuid(UUID creatorAccountUuid) {
        this.creatorAccountUuid = creatorAccountUuid;
    }

    @Override
    public String toString() {
        return "DBItem{" +
                "pk='" + pk + '\'' +
                ", sk='" + sk + '\'' +
                ", entityType=" + entityType +
                ", accountUuid=" + accountUuid +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                ", taskUuid=" + taskUuid +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", commentUuid=" + commentUuid +
                ", body='" + body + '\'' +
                ", creatorAccountUuid=" + creatorAccountUuid +
                '}';
    }

    public static class Builder {

        // **** Composite Primary Key ****
        private String pk; // partition key
        private String sk; // sort key (range key).

        private EntityType entityType;

        // required fields
        // **** Account Attribute ****
        private String name ;
        private String email;
        private String password;
        private Role role;
        private UUID accountUuid;

        // **** Task Attributes ****
        private String title;
        private String description;
        private TaskStatus status;
        private UUID taskUuid;

        // **** Comment Attributes ****
        private String body;
        private UUID commentUuid;
        private UUID creatorAccountUuid;


        public Builder() {
        }

        // **** Composite Primary Key ****
        public Builder withPk(String pk){
            this.pk = pk;
            return this;
        }
        public Builder withSk(String sk){
            this.sk = sk;
            return this;
        }

        // **** Entity type ****
        public Builder withEntityType(EntityType type){
            this.entityType = type;
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

        // **** Task Attributes ****.
        public Builder withTitle(String title){
            this.title = title;
            return this;
        }
        public Builder withDescription(String description){
            this.description = description;
            return this;
        }

        public Builder withStatus(TaskStatus status){
            this.status = status;
            return this;
        }

        public Builder withTaskUuid(UUID uuid){
            this.taskUuid = uuid;
            return this;
        }

        // **** CommentAttributes ****
        public Builder withBody(String body){
            this.body = body;
            return this;
        }
        public Builder withUuid(UUID uuid){
            this.commentUuid = uuid;
            return this;
        }
        public Builder withCreatorAccountUuid(UUID accountUuid){
            this.creatorAccountUuid = accountUuid;
            return this;
        }

        public DBItem build() {
            return new DBItem(this);
        }
    }



}
