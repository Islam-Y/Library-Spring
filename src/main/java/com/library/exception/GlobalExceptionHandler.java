package com.library.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.SQLException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<String> handleNumberFormatException(NumberFormatException e) {
        return ResponseEntity.badRequest().body("{\"error\":\"Invalid ID format\"}");
    }

    @ExceptionHandler(BookServiceException.class)
    public ResponseEntity<String> handleBookServiceException(BookServiceException e) {
        if (e.getCause() instanceof SQLException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Book not found\"}");
        }
        return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception e) {
        return ResponseEntity.internalServerError().body("{\"error\":\"Internal server error\"}");
    }
}