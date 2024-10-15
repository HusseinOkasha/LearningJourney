package com.example.project6.entity;




import com.example.project6.Enum.EntityType;
import com.example.project6.Enum.TaskStatus;
import com.example.project6.util.entityAndDtoMappers.TaskStatusConverter;
import com.example.project6.util.entityAndDtoMappers.UUIDConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.util.UUID;

@DynamoDbBean
public class Task {
    // Composite Primary Key
    private String pk; // partition key
    private String sk; // sort key

    // **** Task Attributes ****
    private UUID taskUuid;
    private String title;
    private String description;
    private TaskStatus status;

    // Constructors
    public Task() {
    }

    private Task(Builder builder) {
        // **** Composite primary key ****
        this.pk = builder.pk;
        this.sk = builder.sk;

        // **** Task Attributes ****.
        this.taskUuid = builder.taskUuid;
        this.description = builder.description;
        this.title = builder.title;
        this.status = builder.status;
    }

    // **** methods ****
    public static Builder builder() {
        // creates and returns an instance of Builder.
        return new Builder();
    }

    @DynamoDbPartitionKey
    public String getPk() {
        return String.format("%s#%s", EntityType.TASK, taskUuid);
    }

    @DynamoDbSortKey
    public String getSk() {
        return String.format("%s#%s", EntityType.TASK, taskUuid);
    }

    @DynamoDbAttribute(value = "task_uuid")
    @DynamoDbConvertedBy(UUIDConverter.class)
    public UUID getTaskUuid() {
        return taskUuid;
    }

    @DynamoDbAttribute(value = "task_title")
    public String getTitle() {
        return title;
    }

    @DynamoDbAttribute(value = "task_description")
    public String getDescription() {
        return description;
    }

    @DynamoDbConvertedBy(TaskStatusConverter.class)
    @DynamoDbAttribute(value = "task_status")
    public TaskStatus getStatus() {
        return status;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    public void setTaskUuid(UUID taskUuid) {
        this.taskUuid = taskUuid;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public static class Builder {
        // **** Composite Key ****
        private String pk;
        private String sk;

        // **** Task Attributes ****
        private String title;
        private String description;
        private TaskStatus status;
        private UUID taskUuid;

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

        // **** Task Attributes ****.
        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withStatus(TaskStatus status) {
            this.status = status;
            return this;
        }

        public Builder withTaskUuid(UUID uuid) {
            this.taskUuid = uuid;
            return this;
        }

        public Task build() {
            return new Task(this);
        }
    }

}
