package com.example.jobboard.exception;

public class ApplicationNotFoundException extends RuntimeException{
    public ApplicationNotFoundException(String message) {
        super(message);
    }
}
