package com.sam.sammart.service;

import com.sam.sammart.dao.ProductDao;
import com.sam.sammart.exception.AppException;
import com.sam.sammart.exception.ForbiddenException;
import com.sam.sammart.exception.NotFoundException;
import com.sam.sammart.exception.ValidationException;
import com.sam.sammart.model.Category;
import com.sam.sammart.model.Product;
import com.sam.sammart.util.ValidationUtil;

import java.math.BigDecimal;
import java.util.List;

/**
 * Catalog and seller listing rules.
 */
public class ProductService {
    private final ProductDao productDao;

    public ProductService(ProductDao productDao) {
        this.productDao = productDao;
    }

    public List<Product> browse(String keyword, Long categoryId) throws AppException {
        String q = keyword == null ? null : keyword.trim();
        if (q != null && q.length() > 80) {
            throw new ValidationException("keyword is too long");
        }
        return productDao.search(q, categoryId, true);
    }

    public Product get(long id) throws AppException {
        return productDao.findById(id).orElseThrow(() -> new NotFoundException("product not found"));
    }

    public List<Product> sellerListings(long sellerId) throws AppException {
        return productDao.findBySeller(sellerId);
    }

    public List<Category> categories() throws AppException {
        return productDao.findCategories();
    }

    /**
     * Creates a listing owned by the authenticated seller.
     */
    public Product create(long sellerId, long categoryId, String name, String description, BigDecimal price,
                          int stock, String imageUrl) throws AppException {
        Product p = build(sellerId, categoryId, name, description, price, stock, imageUrl);
        return productDao.insert(p);
    }

    public void update(long sellerId, long productId, long categoryId, String name, String description,
                       BigDecimal price, int stock, String imageUrl) throws AppException {
        Product existing = get(productId);
        if (existing.getSellerId() != sellerId) {
            throw new ForbiddenException("you do not own this listing");
        }
        Product p = build(sellerId, categoryId, name, description, price, stock, imageUrl);
        p.setId(productId);
        productDao.update(p);
    }

    public void deleteOwn(long sellerId, long productId) throws AppException {
        Product existing = get(productId);
        if (existing.getSellerId() != sellerId) {
            throw new ForbiddenException("you do not own this listing");
        }
        productDao.softDelete(productId);
    }

    public void adminRemove(long productId) throws AppException {
        get(productId);
        productDao.softDelete(productId);
    }

    private Product build(long sellerId, long categoryId, String name, String description, BigDecimal price,
                          int stock, String imageUrl) throws ValidationException {
        Product p = new Product();
        p.setSellerId(sellerId);
        p.setCategoryId(categoryId);
        p.setName(ValidationUtil.requireText(name, "name", 2, 150));
        p.setDescription(ValidationUtil.requireText(description, "description", 8, 2000));
        p.setPrice(ValidationUtil.requireMoney(price, "price"));
        p.setStockQty(stock);
        if (stock < 0) {
            throw new ValidationException("stock must be 0 or more");
        }
        p.setImageUrl(imageUrl == null || imageUrl.isBlank() ? null : imageUrl.trim());
        return p;
    }
}
