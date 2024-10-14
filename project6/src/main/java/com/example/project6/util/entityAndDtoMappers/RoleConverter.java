package com.example.project6.util.entityAndDtoMappers;

import com.example.project6.Enum.Role;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.UUID;

public class RoleConverter implements AttributeConverter<Role> {

    @Override
    public AttributeValue transformFrom(Role role) {
        return AttributeValue.builder().s(role.toString()).build();
    }


    @Override
    public Role transformTo(AttributeValue attributeValue) {
        return Role.valueOf(attributeValue.s());
    }

    @Override
    public EnhancedType<Role> type() {
        return EnhancedType.of(Role.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}
