package com.sam.sammart.exception;

public class NotFoundException extends AppException {
    public NotFoundException(String message) {
        super("NOT_FOUND", message, 404);
    }
}
