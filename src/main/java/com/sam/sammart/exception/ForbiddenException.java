package com.sam.sammart.exception;

public class ForbiddenException extends AppException {
    public ForbiddenException(String message) {
        super("FORBIDDEN", message, 403);
    }
}
