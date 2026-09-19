package com.sam.sammart.dao;

import com.sam.sammart.exception.DaoException;
import com.sam.sammart.model.Order;
import com.sam.sammart.model.OrderItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JdbcOrderDao implements OrderDao {
    private final DataSource dataSource;

    public JdbcOrderDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public long insertOrder(Connection connection, Order order) throws DaoException {
        String sql = "INSERT INTO orders (buyer_id, status, total_amount, payment_ref) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, order.getBuyerId());
            ps.setString(2, order.getStatus());
            ps.setBigDecimal(3, order.getTotalAmount());
            ps.setString(4, order.getPaymentRef());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            throw new DaoException("Order insert returned no id", null);
        } catch (SQLException e) {
            throw new DaoException("Failed to insert order", e);
        }
    }

    @Override
    public void insertItem(Connection connection, OrderItem item) throws DaoException {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, item.getOrderId());
            ps.setLong(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setBigDecimal(4, item.getUnitPrice());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to insert order item", e);
        }
    }

    @Override
    public Optional<Order> findById(long id) throws DaoException {
        return load("WHERE o.id = ?", ps -> ps.setLong(1, id)).stream().findFirst();
    }

    @Override
    public List<Order> findByBuyer(long buyerId) throws DaoException {
        return load("WHERE o.buyer_id = ? ORDER BY o.created_at DESC", ps -> ps.setLong(1, buyerId));
    }

    @Override
    public List<Order> findIncomingForSeller(long sellerId) throws DaoException {
        return load("WHERE EXISTS (SELECT 1 FROM order_items oi JOIN products p ON p.id = oi.product_id "
                + "WHERE oi.order_id = o.id AND p.seller_id = ?) ORDER BY o.created_at DESC",
                ps -> ps.setLong(1, sellerId));
    }

    @Override
    public List<Order> findAll() throws DaoException {
        return load("ORDER BY o.created_at DESC", ps -> { });
    }

    @Override
    public void updateStatus(long orderId, String status) throws DaoException {
        String sql = "UPDATE orders SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to update order status", e);
        }
    }

    @Override
    public boolean buyerPurchasedProduct(long buyerId, long productId, long orderId) throws DaoException {
        String sql = "SELECT 1 FROM orders o JOIN order_items oi ON oi.order_id = o.id "
                + "WHERE o.id = ? AND o.buyer_id = ? AND oi.product_id = ? AND o.status = 'DELIVERED'";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.setLong(2, buyerId);
            ps.setLong(3, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to verify purchase", e);
        }
    }

    private List<Order> load(String where, SqlBinder binder) throws DaoException {
        String sql = "SELECT o.id, o.buyer_id, o.status, o.total_amount, o.payment_ref, o.created_at, "
                + "u.name AS buyer_name, oi.id AS item_id, oi.product_id, oi.quantity, oi.unit_price, "
                + "p.name AS product_name, p.seller_id "
                + "FROM orders o JOIN users u ON u.id = o.buyer_id "
                + "LEFT JOIN order_items oi ON oi.order_id = o.id "
                + "LEFT JOIN products p ON p.id = oi.product_id " + where;
        Map<Long, Order> map = new LinkedHashMap<>();
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long orderId = rs.getLong("id");
                    Order order = map.computeIfAbsent(orderId, id -> {
                        try {
                            Order o = new Order();
                            o.setId(id);
                            o.setBuyerId(rs.getLong("buyer_id"));
                            o.setStatus(rs.getString("status"));
                            o.setTotalAmount(rs.getBigDecimal("total_amount"));
                            o.setPaymentRef(rs.getString("payment_ref"));
                            o.setCreatedAt(DaoFactory.toLocal(rs.getTimestamp("created_at")));
                            o.setBuyerName(rs.getString("buyer_name"));
                            return o;
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    long itemId = rs.getLong("item_id");
                    if (!rs.wasNull()) {
                        OrderItem item = new OrderItem();
                        item.setId(itemId);
                        item.setOrderId(orderId);
                        item.setProductId(rs.getLong("product_id"));
                        item.setQuantity(rs.getInt("quantity"));
                        item.setUnitPrice(rs.getBigDecimal("unit_price"));
                        item.setProductName(rs.getString("product_name"));
                        item.setSellerId(rs.getLong("seller_id"));
                        order.getItems().add(item);
                    }
                }
            }
            return new ArrayList<>(map.values());
        } catch (SQLException | RuntimeException e) {
            throw new DaoException("Failed to load orders", e);
        }
    }

    @FunctionalInterface
    private interface SqlBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }
}
