package com.example.project6.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.example.project6.Enum.EntityType;
import com.example.project6.util.entityAndDtoMappers.UUIDConverter;

import java.util.UUID;

@DynamoDBTable(tableName = "app")
public class Comment {
    // Composite Primary Key
    @DynamoDBHashKey
    private String pk; // partition key
    @DynamoDBRangeKey
    private String sk; // sort key

    // **** Comment Attributes ****
    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = UUIDConverter.class)
    private UUID commentUuid;

    @DynamoDBAttribute
    private String body;

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = UUIDConverter.class)
    private UUID creatorAccountUuid;

    private Comment(Builder builder){
        // **** Composite primary key ****
        this.pk = builder.pk;
        this.sk = builder.sk;

        // **** CommentAttributes ****
        this.commentUuid = builder.commentUuid;
        this.body = builder.body;
        this.creatorAccountUuid = builder.creatorAccountUuid;

    }

    public Comment() {
    }

    // **** methods ****
    public static Builder builder() {
        // creates and returns an instance of Builder.
        return new Builder();
    }

    public String getPk() {
        return pk;
    }

    public String getSk() {
        return sk;
    }

    public UUID getCommentUuid() {
        return commentUuid;
    }

    public String getBody() {
        return body;
    }

    public UUID getCreatorAccountUuid() {
        return creatorAccountUuid;
    }

    public static class Builder {
        // **** Composite Key ****
        private String pk;
        private String sk;

        // **** Comment Attributes ****
        private String body;
        private UUID commentUuid;
        private UUID creatorAccountUuid;


        public Builder() {
        }

        // **** Composite Key ****
        public Builder withPk(String pk){
            this.pk = String.format("%s#%s", EntityType.ACCOUNT, pk);
            return this;
        }
        public Builder withSk(String sk){
            this.sk = String.format("%s#%s", EntityType.ACCOUNT, sk);
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

        public Comment build() {
            return new Comment(this);
        }
    }


}
