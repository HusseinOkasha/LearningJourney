package com.example.EmployeeManager.exception;

import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

public record ApiExceptionDto(String message,
                              HttpStatus httpStatus,
                              ZonedDateTime zonedDateTime) {
}
