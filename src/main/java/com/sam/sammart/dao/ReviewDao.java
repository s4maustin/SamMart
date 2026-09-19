package com.sam.sammart.dao;

import com.sam.sammart.exception.DaoException;
import com.sam.sammart.model.Review;

import java.util.List;

public interface ReviewDao {
    void insert(Review review) throws DaoException;

    List<Review> findByProduct(long productId) throws DaoException;

    boolean existsForUserProduct(long userId, long productId) throws DaoException;
}
