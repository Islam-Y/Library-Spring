package com.library.exception;

public class AuthorServiceException extends RuntimeException {
    public AuthorServiceException(String message) {
        super(message);
    }
}