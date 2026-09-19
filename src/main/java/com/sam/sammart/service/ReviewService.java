package com.sam.sammart.service;

import com.sam.sammart.dao.OrderDao;
import com.sam.sammart.dao.ProductDao;
import com.sam.sammart.dao.ReviewDao;
import com.sam.sammart.dao.UserDao;
import com.sam.sammart.exception.AppException;
import com.sam.sammart.exception.ConflictException;
import com.sam.sammart.exception.ForbiddenException;
import com.sam.sammart.exception.NotFoundException;
import com.sam.sammart.exception.ValidationException;
import com.sam.sammart.model.Review;
import com.sam.sammart.model.User;
import com.sam.sammart.util.ValidationUtil;

import java.util.List;

/**
 * Reviews are allowed only after a delivered purchase; one review per user per product.
 */
public class ReviewService {
    private final ReviewDao reviewDao;
    private final OrderDao orderDao;
    private final ProductDao productDao;
    private final UserDao userDao;

    public ReviewService(ReviewDao reviewDao, OrderDao orderDao, ProductDao productDao, UserDao userDao) {
        this.reviewDao = reviewDao;
        this.orderDao = orderDao;
        this.productDao = productDao;
        this.userDao = userDao;
    }

    public List<Review> forProduct(long productId) throws AppException {
        return reviewDao.findByProduct(productId);
    }

    /**
     * Persists a star rating after verifying delivered-order ownership.
     */
    public void add(long userId, long productId, long orderId, int rating, String comment) throws AppException {
        ValidationUtil.requireRating(rating);
        String text = ValidationUtil.requireText(comment, "comment", 3, 1000);
        productDao.findById(productId).orElseThrow(() -> new NotFoundException("product not found"));
        if (reviewDao.existsForUserProduct(userId, productId)) {
            throw new ConflictException("you already reviewed this product");
        }
        if (!orderDao.buyerPurchasedProduct(userId, productId, orderId)) {
            throw new ForbiddenException("review allowed only on delivered purchases");
        }
        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(productId);
        review.setOrderId(orderId);
        review.setRating(rating);
        review.setComment(text);
        reviewDao.insert(review);
    }

    public List<User> allUsers() throws AppException {
        return userDao.findAll();
    }
}
