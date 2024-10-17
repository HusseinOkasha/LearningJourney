package com.example.project6;

import com.example.project6.Enum.Role;
import com.example.project6.entity.Account;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@Component
public class Util {
    @Value("${authentication.url}")
    private  String authenticationUrl;


    public CreateTableRequest buildCreateTableRequest(){
       return CreateTableRequest
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
                .build();
    }
    public List<Account> buildAccounts(){
        // build admin accounts
        Account admin1 = Account.builder()
                .withName("Hussein")
                .withEmail("e1@email.com")
                .withPassword("123")
                .withRole(Role.ADMIN)
                .build();
        Account admin2 = Account.builder()
                .withName("Mohammed")
                .withEmail("e2@email.com")
                .withPassword("123")
                .withRole(Role.ADMIN)
                .build();

        // build employee accounts.
        Account employee1 = Account.builder()
                .withName("Ahmed")
                .withEmail("e3@email.com")
                .withPassword("123")
                .withRole(Role.ADMIN)
                .build();
        Account employee2 = Account.builder()
                .withName("Abdallah")
                .withEmail("e4@email.com")
                .withPassword("123")
                .withRole(Role.ADMIN)
                .build();
        return List.of(admin1, admin2, employee1, employee2);
    }

    public String attemptAuthenticationWith(Account account) {
        /*
         * helper method attempt authentication with the passed account.
         * it checks that the authentication request was successful
         * by checking that the response status code is 200 OK
         * returns jwt token resulted from the authentication process.
         * */
        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .auth()
                        .preemptive()
                        .basic(account.getEmail(), "123")
                        .when()
                        .post(authenticationUrl)
                        .then()
                        .statusCode(200)
                        .body("accessToken", notNullValue())
                        .extract()
                        .response();

        String accessToken = response
                .jsonPath()
                .getString("accessToken");
        assertThat(accessToken).isNotEmpty();
        return accessToken;
    }
}
