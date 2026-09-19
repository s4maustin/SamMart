package com.sam.sammart;

import com.sam.sammart.dao.DaoFactory;
import com.sam.sammart.dto.CheckoutRequest;
import com.sam.sammart.exception.ConflictException;
import com.sam.sammart.exception.ValidationException;
import com.sam.sammart.model.Product;
import com.sam.sammart.model.User;
import com.sam.sammart.service.AuthService;
import com.sam.sammart.service.CartService;
import com.sam.sammart.service.MockPaymentChannel;
import com.sam.sammart.service.OrderService;
import com.sam.sammart.service.ProductService;
import com.sam.sammart.util.PasswordUtil;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DaoAndCheckoutTest {
    private HikariDataSource ds;
    private DaoFactory factory;
    private long sellerId;
    private long buyerId;
    private long productId;

    @BeforeEach
    void setup() throws Exception {
        ds = TestDb.memory();
        factory = new DaoFactory(ds);
        User seller = user("Priya", "seller@test.local", "SELLER");
        User buyer = user("Arun", "buyer@test.local", "BUYER");
        sellerId = seller.getId();
        buyerId = buyer.getId();
        factory.productDao().insertCategory("Electronics", "electronics");
        Product p = new Product();
        p.setSellerId(sellerId);
        p.setCategoryId(1);
        p.setName("USB Hub");
        p.setDescription("Seven port hub for laptops");
        p.setPrice(new BigDecimal("499.00"));
        p.setStockQty(2);
        factory.productDao().insert(p);
        productId = p.getId();
    }

    @AfterEach
    void tearDown() {
        if (ds != null) {
            ds.close();
        }
    }

    @Test
    void userEmailIsUnique() throws Exception {
        assertTrue(factory.userDao().emailExists("seller@test.local"));
        assertThrows(Exception.class, () -> user("Other", "seller@test.local", "BUYER"));
    }

    @Test
    void productSearchByKeyword() throws Exception {
        ProductService products = new ProductService(factory.productDao());
        assertEquals(1, products.browse("hub", null).size());
        assertEquals(0, products.browse("banana", null).size());
    }

    @Test
    void checkoutDecrementsStockAndClearsCart() throws Exception {
        CartService cart = new CartService(factory.cartDao(), factory.productDao());
        OrderService orders = new OrderService(ds, factory.cartDao(), factory.productDao(), factory.orderDao(),
                factory.supportDao(), new MockPaymentChannel());
        cart.add(buyerId, productId, 2);
        orders.checkout(buyerId, new CheckoutRequest.Builder().confirmPayment(true).build());
        assertEquals(0, factory.productDao().findById(productId).orElseThrow().getStockQty());
        assertTrue(factory.cartDao().findByUser(buyerId).isEmpty());
        assertEquals(1, factory.orderDao().findByBuyer(buyerId).size());
    }

    @Test
    void checkoutRejectsOverSell() throws Exception {
        CartService cart = new CartService(factory.cartDao(), factory.productDao());
        cart.add(buyerId, productId, 2);
        Product update = factory.productDao().findById(productId).orElseThrow();
        update.setStockQty(1);
        factory.productDao().update(update);
        OrderService orders = new OrderService(ds, factory.cartDao(), factory.productDao(), factory.orderDao(),
                factory.supportDao(), new MockPaymentChannel());
        assertThrows(ConflictException.class,
                () -> orders.checkout(buyerId, new CheckoutRequest.Builder().confirmPayment(true).build()));
        assertEquals(1, factory.productDao().findById(productId).orElseThrow().getStockQty());
    }

    @Test
    void checkoutRequiresPaymentConfirmation() {
        OrderService orders = new OrderService(ds, factory.cartDao(), factory.productDao(), factory.orderDao(),
                factory.supportDao(), new MockPaymentChannel());
        assertThrows(ValidationException.class,
                () -> orders.checkout(buyerId, new CheckoutRequest.Builder().confirmPayment(false).build()));
    }

    @Test
    void bcryptRoundTrip() {
        String hash = PasswordUtil.hash("Buyer@123");
        assertTrue(PasswordUtil.verify("Buyer@123", hash));
    }

    @Test
    void registerRejectsAdminRole() {
        AuthService auth = new AuthService(factory.userDao());
        assertThrows(ValidationException.class,
                () -> auth.register("Hack", "hack@test.local", "Password1", "ADMIN"));
    }

    private User user(String name, String email, String role) throws Exception {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPasswordHash(PasswordUtil.hash("Password1"));
        u.setRole(role);
        return factory.userDao().insert(u);
    }
}
