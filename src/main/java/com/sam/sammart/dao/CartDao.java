package com.sam.sammart.dao;

import com.sam.sammart.exception.DaoException;
import com.sam.sammart.model.CartItem;

import java.sql.Connection;
import java.util.List;

public interface CartDao {
    List<CartItem> findByUser(long userId) throws DaoException;

    List<CartItem> findByUser(Connection connection, long userId) throws DaoException;

    void upsert(long userId, long productId, int quantity) throws DaoException;

    void updateQuantity(long userId, long cartItemId, int quantity) throws DaoException;

    void delete(long userId, long cartItemId) throws DaoException;

    void clear(Connection connection, long userId) throws DaoException;
}
