package com.sam.sammart.service;

import com.sam.sammart.dao.UserDao;
import com.sam.sammart.dto.UserResponseDto;
import com.sam.sammart.exception.AppException;
import com.sam.sammart.exception.ConflictException;
import com.sam.sammart.exception.UnauthorizedException;
import com.sam.sammart.exception.ValidationException;
import com.sam.sammart.model.User;
import com.sam.sammart.util.PasswordUtil;
import com.sam.sammart.util.ValidationUtil;

/**
 * Registration and authentication rules. No JDBC here.
 */
public class AuthService {
    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Registers a BUYER or SELLER. ADMIN cannot self-register.
     */
    public UserResponseDto register(String name, String email, String password, String role) throws AppException {
        String safeName = ValidationUtil.requireText(name, "name", 2, 100);
        String safeEmail = ValidationUtil.requireEmail(email);
        String safePassword = ValidationUtil.requireText(password, "password", 8, 72);
        String safeRole = role == null ? "BUYER" : role.trim().toUpperCase();
        if (!"BUYER".equals(safeRole) && !"SELLER".equals(safeRole)) {
            throw new ValidationException("role must be BUYER or SELLER");
        }
        if (userDao.emailExists(safeEmail)) {
            throw new ConflictException("email already registered");
        }
        User user = new User();
        user.setName(safeName);
        user.setEmail(safeEmail);
        user.setPasswordHash(PasswordUtil.hash(safePassword));
        user.setRole(safeRole);
        userDao.insert(user);
        return toDto(user);
    }

    /**
     * Verifies bcrypt credentials and returns a session-safe DTO.
     */
    public User login(String email, String password) throws AppException {
        String safeEmail = ValidationUtil.requireEmail(email);
        if (password == null || password.isBlank()) {
            throw new ValidationException("password is required");
        }
        User user = userDao.findByEmail(safeEmail)
                .orElseThrow(() -> new UnauthorizedException("invalid email or password"));
        if (!user.isActive() || !PasswordUtil.verify(password, user.getPasswordHash())) {
            throw new UnauthorizedException("invalid email or password");
        }
        return user;
    }

    public UserResponseDto toDto(User user) {
        return new UserResponseDto(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
