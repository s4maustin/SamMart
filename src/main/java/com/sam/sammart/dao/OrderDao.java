package com.sam.sammart.dao;

import com.sam.sammart.exception.DaoException;
import com.sam.sammart.model.Order;
import com.sam.sammart.model.OrderItem;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface OrderDao {
    long insertOrder(Connection connection, Order order) throws DaoException;

    void insertItem(Connection connection, OrderItem item) throws DaoException;

    Optional<Order> findById(long id) throws DaoException;

    List<Order> findByBuyer(long buyerId) throws DaoException;

    List<Order> findIncomingForSeller(long sellerId) throws DaoException;

    List<Order> findAll() throws DaoException;

    void updateStatus(long orderId, String status) throws DaoException;

    boolean buyerPurchasedProduct(long buyerId, long productId, long orderId) throws DaoException;
}
