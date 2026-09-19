package com.sam.sammart.exception;

public class DaoException extends AppException {
    public DaoException(String message, Throwable cause) {
        super("DAO_ERROR", message, 500, cause);
    }
}
