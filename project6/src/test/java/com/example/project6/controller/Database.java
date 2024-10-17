package com.example.project6.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;


@Testcontainers
@ActiveProfiles("test")
@Service
public class Database {


    private final DynamoDbClient dynamoDbClient;

    @Container
    private LocalStackContainer localStack ;

    @Autowired
    public Database(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }


    public void start(){
        // create local stack container containing dynamodb service.

        localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.2"))
                .withServices(LocalStackContainer.Service.DYNAMODB);

        localStack.start();
        System.setProperty("amazon.dynamodb.endpoint",
                localStack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString());
    }

    public void  createTable(){
        dynamoDbClient.createTable(CreateTableRequest
                .builder()
                .tableName("app")
                .keySchema(
                        KeySchemaElement.builder()
                                .attributeName("pk")
                                .keyType(KeyType.HASH) // Partition key
                                .build(),
                        KeySchemaElement.builder()
                                .attributeName("sk")
                                .keyType(KeyType.RANGE) // Sort key
                                .build()
                )
                .attributeDefinitions(
                        AttributeDefinition.builder()
                                .attributeName("pk")
                                .attributeType(ScalarAttributeType.S) // String type
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("sk")
                                .attributeType(ScalarAttributeType.S) // String type
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("role")
                                .attributeType(ScalarAttributeType.S) // ROLE_INDEX (GSI) partition key
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("email")
                                .attributeType(ScalarAttributeType.S) // EMAIL_INDEX (GSI) partition key
                                .build()
                )
                .globalSecondaryIndexes(GlobalSecondaryIndex.builder()
                                .indexName("EMAIL_INDEX")
                                .keySchema(
                                        KeySchemaElement.builder()
                                                .attributeName("email")  // EMAIL_INDEX partition key.
                                                .keyType(KeyType.HASH)
                                                .build()
                                )
                                .projection(Projection.builder()
                                        .projectionType(ProjectionType.ALL)
                                        .build())
                                .provisionedThroughput(ProvisionedThroughput.builder()
                                        .readCapacityUnits((long) 5)
                                        .writeCapacityUnits((long) 5)
                                        .build()
                                )
                                .build(),
                        GlobalSecondaryIndex.builder()
                                .indexName("ROLE_INDEX")
                                .keySchema(
                                        KeySchemaElement.builder()
                                                .attributeName("role")  // ROLE_INDEX partition key.
                                                .keyType(KeyType.HASH)
                                                .build()
                                )
                                .provisionedThroughput(ProvisionedThroughput.builder()
                                        .readCapacityUnits((long) 5)
                                        .writeCapacityUnits((long) 5)
                                        .build()
                                )
                                .projection(Projection.builder()
                                        .projectionType(ProjectionType.ALL)
                                        .build())
                                .build()
                ).provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits((long) 5)
                        .writeCapacityUnits((long) 5)
                        .build()
                )
                .build());
    }

    public void  init(){

    }
}
