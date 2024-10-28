package com.example.project6;

import com.example.project6.Enum.Role;
import com.example.project6.Enum.TaskStatus;
import com.example.project6.entity.Account;
import com.example.project6.entity.AccountTaskLink;
import com.example.project6.entity.Task;
import com.example.project6.entity.TaskAccountLink;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@Component
public class Util {
    @Value("${authentication.url}")
    private String authenticationUrl;

    public CreateTableRequest buildCreateTableRequest() {
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

    public static List<Account> buildAccounts() {
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
                .withRole(Role.EMPLOYEE)
                .build();
        Account employee2 = Account.builder()
                .withName("Abdallah")
                .withEmail("e4@email.com")
                .withPassword("123")
                .withRole(Role.EMPLOYEE)
                .build();
        return List.of(admin1, admin2, employee1, employee2);
    }

    public static List<Task> buildSampleTasks() {
        // helper function creates a list of 2 sample tasks
        Task task1 = Task.builder()
                .withTitle("fixBugs")
                .withDescription("fixing bugs in bugy controller.")
                .withStatus(TaskStatus.TODO)
                .build();

        Task task2 = Task.builder()
                .withTitle("add some tests")
                .withDescription("add tests for X controller.")
                .withStatus(TaskStatus.IN_PROGRESS)
                .build();
        return List.of(task1, task2);
    }

    public static AccountTaskLink buildAccountTaskLinkWith(Account account, Task task) {
        return AccountTaskLink.builder()
                .withAccountUuid(account.getAccountUuid())
                .withAccountName(account.getName())
                .withTaskUuid(task.getTaskUuid())
                .withTaskTitle(task.getTitle())
                .build();

    }

    public static TaskAccountLink buildTaskAccountLinkWith(Account account, Task task) {
        return TaskAccountLink.builder()
                .withAccountUuid(account.getAccountUuid())
                .withAccountName(account.getName())
                .withTaskUuid(task.getTaskUuid())
                .withTaskTitle(task.getTitle())
                .build();

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

    public static Task updateTask(Task task) {
        /*
         * Helper method that takes a task updates its description, title, and status.
         * Then returns the task after performing updates on it.
         * */

        task.setTitle("updated Title");
        task.setDescription("updated task description.");
        for (TaskStatus status : TaskStatus.values()) {
            if (task.getStatus() != status) {
                task.setStatus(status);
            }
        }
        return task;
    }

    public static Map<String, Object> buildUpdateTaskRequestBody(Task task) {
        /*
         * Arguments:
         *   - task from which we will take data to create update Task request body.
         * Return value:
         *   - A map contains, key: the fields , value: their values .
         * */
        return Map.of(
                "title", task.getTitle(),
                "description", task.getDescription(),
                "status", task.getStatus());
    }

    public static List<String> getInvalidTaskDescriptions() {
        // helper method.
        // returns a list of all possible invalid descriptions
        return List.of("");
    }

    public static List<String> getInvalidTaskTitles() {
        // helper method.
        // returns a list of all possible invalid titles.
        return List.of("");
    }

    public static List<String> getInvalidTaskStatus() {
        // helper method.
        // returns a list of all possible invalid task status.
        return List.of("", "randomString");
    }
    public static Map<String, String> updateRequestBody(Map<String, String> requestBody,
                                                        Map<String, String> updates, List<String> toBeDeletedKeys){
        Map<String, String> updatedRequestBody = new HashMap<>(requestBody);
        updatedRequestBody.putAll(updates);
        toBeDeletedKeys.forEach(updatedRequestBody::remove);
        return updatedRequestBody;
    }
    public static Arguments generateArgumentsFrom(Map<String, String> requestBody, Map<String, String > errors){
        return Arguments.of(requestBody, errors);
    }

}
