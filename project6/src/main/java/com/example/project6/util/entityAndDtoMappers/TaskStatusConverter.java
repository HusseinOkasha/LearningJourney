package com.example.project6.util.entityAndDtoMappers;

import com.example.project6.Enum.TaskStatus;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class TaskStatusConverter implements AttributeConverter<TaskStatus> {
    @Override
    public AttributeValue transformFrom(TaskStatus taskStatus) {
        return AttributeValue.builder().s(taskStatus.toString()).build();
    }

    @Override
    public TaskStatus transformTo(AttributeValue attributeValue) {
        return TaskStatus.valueOf(attributeValue.s());
    }

    @Override
    public EnhancedType<TaskStatus> type() {
        return EnhancedType.of(TaskStatus.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}
