package com.sam.sammart.exception;

public class UnauthorizedException extends AppException {
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message, 401);
    }
}
