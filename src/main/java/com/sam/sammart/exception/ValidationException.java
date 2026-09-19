package com.sam.sammart.exception;

public class ValidationException extends AppException {
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message, 400);
    }
}
