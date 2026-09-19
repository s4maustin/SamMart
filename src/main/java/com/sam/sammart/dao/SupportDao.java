package com.sam.sammart.dao;

import com.sam.sammart.exception.DaoException;

import java.math.BigDecimal;
import java.sql.Connection;

public interface SupportDao {
    void insertPayment(Connection connection, long orderId, BigDecimal amount, String status, String ref)
            throws DaoException;

    void insertMovement(Connection connection, long productId, int delta, String reason, Long orderId)
            throws DaoException;

    void insertAudit(Long actorId, String action, String entityType, Long entityId, String details) throws DaoException;

    boolean ping() throws DaoException;
}
