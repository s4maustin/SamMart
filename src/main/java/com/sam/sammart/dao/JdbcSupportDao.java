package com.sam.sammart.dao;

import com.sam.sammart.exception.DaoException;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class JdbcSupportDao implements SupportDao {
    private final DataSource dataSource;

    public JdbcSupportDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insertPayment(Connection connection, long orderId, BigDecimal amount, String status, String ref)
            throws DaoException {
        String sql = "INSERT INTO payments (order_id, amount, method, status, transaction_ref) VALUES (?, ?, 'MOCK', ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, status);
            ps.setString(4, ref);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to insert payment", e);
        }
    }

    @Override
    public void insertMovement(Connection connection, long productId, int delta, String reason, Long orderId)
            throws DaoException {
        String sql = "INSERT INTO inventory_movements (product_id, delta, reason, order_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.setInt(2, delta);
            ps.setString(3, reason);
            if (orderId == null) {
                ps.setNull(4, Types.BIGINT);
            } else {
                ps.setLong(4, orderId);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to insert inventory movement", e);
        }
    }

    @Override
    public void insertAudit(Long actorId, String action, String entityType, Long entityId, String details)
            throws DaoException {
        String sql = "INSERT INTO audit_logs (actor_id, action, entity_type, entity_id, details) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (actorId == null) {
                ps.setNull(1, Types.BIGINT);
            } else {
                ps.setLong(1, actorId);
            }
            ps.setString(2, action);
            ps.setString(3, entityType);
            if (entityId == null) {
                ps.setNull(4, Types.BIGINT);
            } else {
                ps.setLong(4, entityId);
            }
            ps.setString(5, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to insert audit log", e);
        }
    }

    @Override
    public boolean ping() throws DaoException {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT 1");
             ResultSet rs = ps.executeQuery()) {
            return rs.next();
        } catch (SQLException e) {
            throw new DaoException("Health ping failed", e);
        }
    }
}
