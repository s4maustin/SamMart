package com.sam.sammart.util;

import com.sam.sammart.exception.ValidationException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Shared input guards used at the top of every service method.
 */
public final class ValidationUtil {
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private ValidationUtil() {
    }

    public static String requireText(String value, String field, int min, int max) throws ValidationException {
        if (value == null || value.trim().length() < min) {
            throw new ValidationException(field + " must be at least " + min + " characters");
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            throw new ValidationException(field + " must be at most " + max + " characters");
        }
        return trimmed;
    }

    public static String requireEmail(String email) throws ValidationException {
        String value = requireText(email, "email", 5, 255);
        if (!EMAIL.matcher(value).matches()) {
            throw new ValidationException("email is invalid");
        }
        return value.toLowerCase();
    }

    public static int requirePositiveInt(int value, String field) throws ValidationException {
        if (value < 1) {
            throw new ValidationException(field + " must be at least 1");
        }
        return value;
    }

    public static BigDecimal requireMoney(BigDecimal value, String field) throws ValidationException {
        if (value == null || value.compareTo(new BigDecimal("0.01")) < 0) {
            throw new ValidationException(field + " must be at least 0.01");
        }
        if (value.scale() > 2) {
            throw new ValidationException(field + " cannot have more than 2 decimal places");
        }
        return value;
    }

    public static int requireRating(int rating) throws ValidationException {
        if (rating < 1 || rating > 5) {
            throw new ValidationException("rating must be between 1 and 5");
        }
        return rating;
    }

    public static String likeContains(String keyword) {
        return "%" + keyword.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }
}
