package com.sam.sammart.service;

import com.sam.sammart.dao.CartDao;
import com.sam.sammart.dao.ProductDao;
import com.sam.sammart.exception.AppException;
import com.sam.sammart.exception.ValidationException;
import com.sam.sammart.model.CartItem;
import com.sam.sammart.model.Product;
import com.sam.sammart.util.ValidationUtil;

import java.math.BigDecimal;
import java.util.List;

/**
 * Cart quantity rules against live stock.
 */
public class CartService {
    private final CartDao cartDao;
    private final ProductDao productDao;

    public CartService(CartDao cartDao, ProductDao productDao) {
        this.cartDao = cartDao;
        this.productDao = productDao;
    }

    public List<CartItem> list(long userId) throws AppException {
        return cartDao.findByUser(userId);
    }

    public BigDecimal total(List<CartItem> items) {
        return items.stream().map(CartItem::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Adds or replaces a cart line after checking product existence and stock.
     */
    public void add(long userId, long productId, int quantity) throws AppException {
        int qty = ValidationUtil.requirePositiveInt(quantity, "quantity");
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new ValidationException("product not found"));
        if (!product.isActive()) {
            throw new ValidationException("product is not available");
        }
        if (qty > product.getStockQty()) {
            throw new ValidationException("quantity exceeds stock");
        }
        cartDao.upsert(userId, productId, qty);
    }

    public void update(long userId, long cartItemId, int quantity) throws AppException {
        int qty = ValidationUtil.requirePositiveInt(quantity, "quantity");
        cartDao.updateQuantity(userId, cartItemId, qty);
    }

    public void remove(long userId, long cartItemId) throws AppException {
        cartDao.delete(userId, cartItemId);
    }
}
