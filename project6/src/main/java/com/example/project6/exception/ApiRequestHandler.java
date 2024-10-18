package com.example.project6.exception;


import com.example.project6.dto.ApiExceptionDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@ControllerAdvice
public class ApiRequestHandler {

    @ExceptionHandler(value = {NotFoundException.class})
    public ResponseEntity<Object> handleNotFoundException(NotFoundException e) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        ApiExceptionDto apiExceptionDto = new ApiExceptionDto(
                e.getMessage(), httpStatus, ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(apiExceptionDto, httpStatus);
    }
    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        ApiExceptionDto apiExceptionDto = new ApiExceptionDto(
                e.getMessage(), httpStatus, ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(apiExceptionDto, httpStatus);
    }
    @ExceptionHandler(value = {AccessDeniedException.class})
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException e) {
        HttpStatus httpStatus = HttpStatus.FORBIDDEN;
        ApiExceptionDto apiExceptionDto = new ApiExceptionDto(
                e.getMessage(), httpStatus, ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(apiExceptionDto, httpStatus);
    }

}