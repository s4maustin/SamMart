package com.sam.sammart.exception;

public class ConflictException extends AppException {
    public ConflictException(String message) {
        super("CONFLICT", message, 409);
    }
}
