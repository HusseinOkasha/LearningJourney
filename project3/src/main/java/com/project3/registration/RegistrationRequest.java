package com.project3.registration;

public record RegistrationRequest(String firstName,
                                  String lastName,
                                  String email,
                                  String password) {

}
