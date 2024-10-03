package com.example.project6.dto;

import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

public record ApiExceptionDto(String message,
                              HttpStatus httpStatus,
                              ZonedDateTime zonedDateTime) {
}