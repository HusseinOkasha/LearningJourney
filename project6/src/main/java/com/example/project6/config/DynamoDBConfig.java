package com.example.project6.config;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.example.project6.entity.Account;
import com.example.project6.entity.AccountTaskLink;
import com.example.project6.entity.Task;
import com.example.project6.entity.TaskAccountLink;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;


@Configuration
public class DynamoDBConfig {

    @Value("${amazon.dynamodb.endpoint}")
    private String amazonDynamoDBEndpoint;

    @Value("${amazon.aws.accesskey}")
    private String amazonAWSAccessKey;

    @Value("${amazon.aws.secretkey}")
    private String amazonAWSSecretKey;

    @Value("${amazon.aws.region}")
    private String region;

    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.of(region))// Your AWS region
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(amazonAWSAccessKey, amazonAWSSecretKey)
                )).endpointOverride(URI.create(amazonDynamoDBEndpoint))
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    @Bean
    public DynamoDbTable<Account> accountTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table("app", TableSchema.fromBean(Account.class));
    }

//    @Bean
//    public DynamoDbTable<Task> taskTable(DynamoDbEnhancedClient enhancedClient) {
//        return enhancedClient.table("app", TableSchema.fromBean(Task.class));
//    }
//
//    @Bean
//    public DynamoDbTable<AccountTaskLink> accountTaskTable(DynamoDbEnhancedClient enhancedClient) {
//        return enhancedClient.table("app", TableSchema.fromBean(AccountTaskLink.class));
//    }
//    @Bean
//    public DynamoDbTable<TaskAccountLink> taskAccountTable(DynamoDbEnhancedClient enhancedClient) {
//        return enhancedClient.table("app", TableSchema.fromBean(TaskAccountLink.class));
//    }
    @Bean
    public DynamoDBMapper dynamoDBMapper() {
        return new DynamoDBMapper(buildAmazonDynamoDB());
    }

    private AmazonDynamoDB buildAmazonDynamoDB() {
        return AmazonDynamoDBClientBuilder
                .standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(
                                amazonDynamoDBEndpoint,
                                region
                        )
                )
                .withCredentials(
                        new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(
                                        amazonAWSAccessKey,
                                        amazonAWSSecretKey
                                )
                        )
                )
                .build();
    }
}