package com.sam.sammart.dao;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Factory for JDBC DAO implementations (Factory pattern).
 */
public class DaoFactory {
    private final UserDao userDao;
    private final ProductDao productDao;
    private final CartDao cartDao;
    private final OrderDao orderDao;
    private final ReviewDao reviewDao;
    private final SupportDao supportDao;

    public DaoFactory(DataSource dataSource) {
        this.userDao = new JdbcUserDao(dataSource);
        this.productDao = new JdbcProductDao(dataSource);
        this.cartDao = new JdbcCartDao(dataSource);
        this.orderDao = new JdbcOrderDao(dataSource);
        this.reviewDao = new JdbcReviewDao(dataSource);
        this.supportDao = new JdbcSupportDao(dataSource);
    }

    public UserDao userDao() {
        return userDao;
    }

    public ProductDao productDao() {
        return productDao;
    }

    public CartDao cartDao() {
        return cartDao;
    }

    public OrderDao orderDao() {
        return orderDao;
    }

    public ReviewDao reviewDao() {
        return reviewDao;
    }

    public SupportDao supportDao() {
        return supportDao;
    }

    static LocalDateTime toLocal(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
