package com.sam.sammart.dao;

import com.sam.sammart.exception.DaoException;
import com.sam.sammart.model.Category;
import com.sam.sammart.model.Product;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface ProductDao {
    Product insert(Product product) throws DaoException;

    void update(Product product) throws DaoException;

    void softDelete(long productId) throws DaoException;

    Optional<Product> findById(long id) throws DaoException;

    List<Product> search(String keyword, Long categoryId, boolean activeOnly) throws DaoException;

    List<Product> findBySeller(long sellerId) throws DaoException;

    List<Category> findCategories() throws DaoException;

    void insertCategory(String name, String slug) throws DaoException;

    int lockAndGetStock(Connection connection, long productId) throws DaoException;

    void decrementStock(Connection connection, long productId, int qty) throws DaoException;

    void incrementStock(Connection connection, long productId, int qty) throws DaoException;
}
