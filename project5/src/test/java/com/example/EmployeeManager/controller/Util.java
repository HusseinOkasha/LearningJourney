package com.example.EmployeeManager.controller;

import com.example.EmployeeManager.model.Account;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@Component
public class Util {

    @Value("${authentication.url}")
    private  String authenticationUrl;

    public String attemptAuthenticationWith(Account account ) {
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
