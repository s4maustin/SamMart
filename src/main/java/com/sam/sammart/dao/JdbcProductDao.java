package com.sam.sammart.dao;

import com.sam.sammart.exception.DaoException;
import com.sam.sammart.model.Category;
import com.sam.sammart.model.Product;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcProductDao implements ProductDao {
    private static final String SELECT = "SELECT p.id, p.seller_id, p.category_id, p.name, p.description, p.price, "
            + "p.stock_qty, p.image_url, p.is_active, p.created_at, u.name AS seller_name, c.name AS category_name, "
            + "COALESCE(v.avg_rating, 0) AS avg_rating, COALESCE(v.review_count, 0) AS review_count "
            + "FROM products p JOIN users u ON u.id = p.seller_id JOIN categories c ON c.id = p.category_id "
            + "LEFT JOIN v_product_rating v ON v.product_id = p.id ";

    private final DataSource dataSource;

    public JdbcProductDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Product insert(Product product) throws DaoException {
        String sql = "INSERT INTO products (seller_id, category_id, name, description, price, stock_qty, image_url) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindProduct(ps, product);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    product.setId(keys.getLong(1));
                }
            }
            return product;
        } catch (SQLException e) {
            throw new DaoException("Failed to insert product", e);
        }
    }

    @Override
    public void update(Product product) throws DaoException {
        String sql = "UPDATE products SET category_id = ?, name = ?, description = ?, price = ?, stock_qty = ?, "
                + "image_url = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND seller_id = ?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, product.getCategoryId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setBigDecimal(4, product.getPrice());
            ps.setInt(5, product.getStockQty());
            ps.setString(6, product.getImageUrl());
            ps.setLong(7, product.getId());
            ps.setLong(8, product.getSellerId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to update product", e);
        }
    }

    @Override
    public void softDelete(long productId) throws DaoException {
        String sql = "UPDATE products SET is_active = FALSE, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to remove listing", e);
        }
    }

    @Override
    public Optional<Product> findById(long id) throws DaoException {
        String sql = SELECT + "WHERE p.id = ?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to load product", e);
        }
    }

    @Override
    public List<Product> search(String keyword, Long categoryId, boolean activeOnly) throws DaoException {
        StringBuilder sql = new StringBuilder(SELECT);
        sql.append("WHERE 1=1 ");
        if (activeOnly) {
            sql.append("AND p.is_active = TRUE ");
        }
        if (categoryId != null) {
            sql.append("AND p.category_id = ? ");
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (LOWER(p.name) LIKE LOWER(?) ESCAPE '\\' OR LOWER(p.description) LIKE LOWER(?) ESCAPE '\\') ");
        }
        sql.append("ORDER BY p.created_at DESC");
        List<Product> list = new ArrayList<>();
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int i = 1;
            if (categoryId != null) {
                ps.setLong(i++, categoryId);
            }
            if (keyword != null && !keyword.isBlank()) {
                String like = com.sam.sammart.util.ValidationUtil.likeContains(keyword);
                ps.setString(i++, like);
                ps.setString(i, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Failed to search products", e);
        }
    }

    @Override
    public List<Product> findBySeller(long sellerId) throws DaoException {
        String sql = SELECT + "WHERE p.seller_id = ? ORDER BY p.created_at DESC";
        List<Product> list = new ArrayList<>();
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Failed to list seller products", e);
        }
    }

    @Override
    public List<Category> findCategories() throws DaoException {
        String sql = "SELECT id, name, slug FROM categories ORDER BY name";
        List<Category> list = new ArrayList<>();
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Category cat = new Category();
                cat.setId(rs.getLong("id"));
                cat.setName(rs.getString("name"));
                cat.setSlug(rs.getString("slug"));
                list.add(cat);
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Failed to list categories", e);
        }
    }

    @Override
    public void insertCategory(String name, String slug) throws DaoException {
        String sql = "INSERT INTO categories (name, slug) VALUES (?, ?)";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, slug);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to insert category", e);
        }
    }

    @Override
    public int lockAndGetStock(Connection connection, long productId) throws DaoException {
        String sql = "SELECT stock_qty FROM products WHERE id = ? FOR UPDATE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("Product missing during stock lock", null);
                }
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to lock stock", e);
        }
    }

    @Override
    public void decrementStock(Connection connection, long productId, int qty) throws DaoException {
        String sql = "UPDATE products SET stock_qty = stock_qty - ?, updated_at = CURRENT_TIMESTAMP "
                + "WHERE id = ? AND stock_qty >= ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setLong(2, productId);
            ps.setInt(3, qty);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new DaoException("Stock decrement rejected", null);
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to decrement stock", e);
        }
    }

    @Override
    public void incrementStock(Connection connection, long productId, int qty) throws DaoException {
        String sql = "UPDATE products SET stock_qty = stock_qty + ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setLong(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to increment stock", e);
        }
    }

    private void bindProduct(PreparedStatement ps, Product product) throws SQLException {
        ps.setLong(1, product.getSellerId());
        ps.setLong(2, product.getCategoryId());
        ps.setString(3, product.getName());
        ps.setString(4, product.getDescription());
        ps.setBigDecimal(5, product.getPrice());
        ps.setInt(6, product.getStockQty());
        ps.setString(7, product.getImageUrl());
    }

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setSellerId(rs.getLong("seller_id"));
        p.setCategoryId(rs.getLong("category_id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStockQty(rs.getInt("stock_qty"));
        p.setImageUrl(rs.getString("image_url"));
        p.setActive(rs.getBoolean("is_active"));
        p.setCreatedAt(DaoFactory.toLocal(rs.getTimestamp("created_at")));
        p.setSellerName(rs.getString("seller_name"));
        p.setCategoryName(rs.getString("category_name"));
        p.setAvgRating(rs.getBigDecimal("avg_rating"));
        p.setReviewCount(rs.getInt("review_count"));
        return p;
    }
}
