package com.sam.sammart.dao;

import com.sam.sammart.exception.DaoException;
import com.sam.sammart.model.User;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface UserDao {
    User insert(User user) throws DaoException;

    Optional<User> findByEmail(String email) throws DaoException;

    Optional<User> findById(long id) throws DaoException;

    List<User> findAll() throws DaoException;

    boolean emailExists(String email) throws DaoException;
}
