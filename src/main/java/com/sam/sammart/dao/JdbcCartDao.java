package com.sam.sammart.dao;

import com.sam.sammart.exception.DaoException;
import com.sam.sammart.model.CartItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcCartDao implements CartDao {
    private static final String SELECT = "SELECT ci.id, ci.user_id, ci.product_id, ci.quantity, p.name AS product_name, "
            + "p.price AS unit_price, p.stock_qty FROM cart_items ci JOIN products p ON p.id = ci.product_id ";

    private final DataSource dataSource;

    public JdbcCartDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<CartItem> findByUser(long userId) throws DaoException {
        try (Connection c = dataSource.getConnection()) {
            return findByUser(c, userId);
        } catch (SQLException e) {
            throw new DaoException("Failed to open connection for cart", e);
        }
    }

    @Override
    public List<CartItem> findByUser(Connection connection, long userId) throws DaoException {
        String sql = SELECT + "WHERE ci.user_id = ? ORDER BY ci.id";
        List<CartItem> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Failed to load cart", e);
        }
    }

    @Override
    public void upsert(long userId, long productId, int quantity) throws DaoException {
        String sql = "MERGE INTO cart_items (user_id, product_id, quantity) KEY (user_id, product_id) "
                + "VALUES (?, ?, ?)";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            ps.setInt(3, quantity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to upsert cart item", e);
        }
    }

    @Override
    public void updateQuantity(long userId, long cartItemId, int quantity) throws DaoException {
        String sql = "UPDATE cart_items SET quantity = ? WHERE id = ? AND user_id = ?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setLong(2, cartItemId);
            ps.setLong(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to update cart quantity", e);
        }
    }

    @Override
    public void delete(long userId, long cartItemId) throws DaoException {
        String sql = "DELETE FROM cart_items WHERE id = ? AND user_id = ?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, cartItemId);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to delete cart item", e);
        }
    }

    @Override
    public void clear(Connection connection, long userId) throws DaoException {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to clear cart", e);
        }
    }

    private CartItem map(ResultSet rs) throws SQLException {
        CartItem item = new CartItem();
        item.setId(rs.getLong("id"));
        item.setUserId(rs.getLong("user_id"));
        item.setProductId(rs.getLong("product_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setProductName(rs.getString("product_name"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setStockQty(rs.getInt("stock_qty"));
        return item;
    }
}
