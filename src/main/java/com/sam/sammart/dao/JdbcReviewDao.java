package com.sam.sammart.dao;

import com.sam.sammart.exception.DaoException;
import com.sam.sammart.model.Review;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcReviewDao implements ReviewDao {
    private final DataSource dataSource;

    public JdbcReviewDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(Review review) throws DaoException {
        String sql = "INSERT INTO reviews (product_id, user_id, order_id, rating, comment) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, review.getProductId());
            ps.setLong(2, review.getUserId());
            ps.setLong(3, review.getOrderId());
            ps.setInt(4, review.getRating());
            ps.setString(5, review.getComment());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to insert review", e);
        }
    }

    @Override
    public List<Review> findByProduct(long productId) throws DaoException {
        String sql = "SELECT r.id, r.product_id, r.user_id, r.order_id, r.rating, r.comment, r.created_at, u.name AS user_name "
                + "FROM reviews r JOIN users u ON u.id = r.user_id WHERE r.product_id = ? ORDER BY r.created_at DESC";
        List<Review> list = new ArrayList<>();
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review r = new Review();
                    r.setId(rs.getLong("id"));
                    r.setProductId(rs.getLong("product_id"));
                    r.setUserId(rs.getLong("user_id"));
                    r.setOrderId(rs.getLong("order_id"));
                    r.setRating(rs.getInt("rating"));
                    r.setComment(rs.getString("comment"));
                    r.setCreatedAt(DaoFactory.toLocal(rs.getTimestamp("created_at")));
                    r.setUserName(rs.getString("user_name"));
                    list.add(r);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Failed to list reviews", e);
        }
    }

    @Override
    public boolean existsForUserProduct(long userId, long productId) throws DaoException {
        String sql = "SELECT 1 FROM reviews WHERE user_id = ? AND product_id = ?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to check review uniqueness", e);
        }
    }
}
