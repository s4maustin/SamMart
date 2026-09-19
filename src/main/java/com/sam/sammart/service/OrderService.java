package com.sam.sammart.service;

import com.sam.sammart.dao.CartDao;
import com.sam.sammart.dao.OrderDao;
import com.sam.sammart.dao.ProductDao;
import com.sam.sammart.dao.SupportDao;
import com.sam.sammart.dto.CheckoutRequest;
import com.sam.sammart.exception.AppException;
import com.sam.sammart.exception.ConflictException;
import com.sam.sammart.exception.ForbiddenException;
import com.sam.sammart.exception.NotFoundException;
import com.sam.sammart.exception.ValidationException;
import com.sam.sammart.model.CartItem;
import com.sam.sammart.model.Order;
import com.sam.sammart.model.OrderItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * Place-order orchestration inside a single JDBC transaction.
 */
public class OrderService {
    private static final Set<String> FLOW = Set.of("PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED");

    private final DataSource dataSource;
    private final CartDao cartDao;
    private final ProductDao productDao;
    private final OrderDao orderDao;
    private final SupportDao supportDao;
    private final PaymentChannel paymentChannel;

    public OrderService(DataSource dataSource, CartDao cartDao, ProductDao productDao, OrderDao orderDao,
                        SupportDao supportDao, PaymentChannel paymentChannel) {
        this.dataSource = dataSource;
        this.cartDao = cartDao;
        this.productDao = productDao;
        this.orderDao = orderDao;
        this.supportDao = supportDao;
        this.paymentChannel = paymentChannel;
    }

    /**
     * Locks product rows, decrements stock, writes order + items + payment + inventory, then clears the cart.
     */
    public Order checkout(long buyerId, CheckoutRequest request) throws AppException {
        if (request == null || !request.isConfirmPayment()) {
            throw new ValidationException("mock payment confirmation is required");
        }
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                List<CartItem> cart = cartDao.findByUser(connection, buyerId);
                if (cart.isEmpty()) {
                    throw new ValidationException("cart is empty");
                }
                BigDecimal total = BigDecimal.ZERO;
                for (CartItem line : cart) {
                    int stock = productDao.lockAndGetStock(connection, line.getProductId());
                    if (line.getQuantity() > stock) {
                        throw new ConflictException("insufficient stock for " + line.getProductName());
                    }
                    total = total.add(line.lineTotal());
                }
                PaymentChannel.PaymentResult pay = paymentChannel.charge(total);
                if (!pay.isSuccess()) {
                    throw new ConflictException("mock payment failed");
                }
                Order order = new Order();
                order.setBuyerId(buyerId);
                order.setStatus("PENDING");
                order.setTotalAmount(total);
                order.setPaymentRef(pay.getReference());
                long orderId = orderDao.insertOrder(connection, order);
                order.setId(orderId);
                for (CartItem line : cart) {
                    OrderItem item = new OrderItem();
                    item.setOrderId(orderId);
                    item.setProductId(line.getProductId());
                    item.setQuantity(line.getQuantity());
                    item.setUnitPrice(line.getUnitPrice());
                    orderDao.insertItem(connection, item);
                    productDao.decrementStock(connection, line.getProductId(), line.getQuantity());
                    supportDao.insertMovement(connection, line.getProductId(), -line.getQuantity(), "SALE", orderId);
                    order.getItems().add(item);
                }
                supportDao.insertPayment(connection, orderId, total, "SUCCESS", pay.getReference());
                cartDao.clear(connection, buyerId);
                connection.commit();
                return orderDao.findById(orderId).orElse(order);
            } catch (AppException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new AppException("DAO_ERROR", "checkout transaction failed", 500, e);
        }
    }

    public List<Order> buyerHistory(long buyerId) throws AppException {
        return orderDao.findByBuyer(buyerId);
    }

    public List<Order> sellerIncoming(long sellerId) throws AppException {
        return orderDao.findIncomingForSeller(sellerId);
    }

    public List<Order> allOrders() throws AppException {
        return orderDao.findAll();
    }

    public Order get(long id) throws AppException {
        return orderDao.findById(id).orElseThrow(() -> new NotFoundException("order not found"));
    }

    /**
     * Optional O2 workflow: Pending → Confirmed → Shipped → Delivered (or Cancelled from Pending).
     */
    public void changeStatus(long actorId, String actorRole, long orderId, String next) throws AppException {
        if (!FLOW.contains(next)) {
            throw new ValidationException("invalid status");
        }
        Order order = get(orderId);
        if ("BUYER".equals(actorRole) && !"CANCELLED".equals(next)) {
            throw new ForbiddenException("buyers cannot advance fulfillment");
        }
        if ("SELLER".equals(actorRole)) {
            boolean owns = order.getItems().stream().anyMatch(i -> i.getSellerId() == actorId);
            if (!owns) {
                throw new ForbiddenException("not a seller on this order");
            }
        }
        orderDao.updateStatus(orderId, next);
        supportDao.insertAudit(actorId, "ORDER_STATUS", "orders", orderId, next);
    }
}
