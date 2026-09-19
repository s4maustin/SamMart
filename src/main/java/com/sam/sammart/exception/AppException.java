package com.sam.sammart.exception;

/**
 * Base checked exception for SamMart business and persistence failures.
 */
public class AppException extends Exception {
    private final int status;
    private final String code;

    public AppException(String code, String message, int status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public AppException(String code, String message, int status, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
