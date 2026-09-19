package com.sam.sammart.controller;

import com.sam.sammart.dto.CheckoutRequest;
import com.sam.sammart.exception.AppException;
import com.sam.sammart.listener.DataSourceListener;
import com.sam.sammart.model.CartItem;
import com.sam.sammart.model.User;
import com.sam.sammart.service.CartService;
import com.sam.sammart.service.OrderService;
import com.sam.sammart.service.ReviewService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/cart", "/checkout", "/orders", "/review"})
public class BuyerServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        CartService cart = (CartService) getServletContext().getAttribute(DataSourceListener.CART);
        OrderService orders = (OrderService) getServletContext().getAttribute(DataSourceListener.ORDERS);
        try {
            switch (req.getServletPath()) {
                case "/cart":
                    List<CartItem> items = cart.list(user.getId());
                    req.setAttribute("items", items);
                    req.setAttribute("total", cart.total(items));
                    req.getRequestDispatcher("/WEB-INF/jsp/cart.jsp").forward(req, resp);
                    break;
                case "/checkout":
                    items = cart.list(user.getId());
                    req.setAttribute("items", items);
                    req.setAttribute("total", cart.total(items));
                    req.getRequestDispatcher("/WEB-INF/jsp/checkout.jsp").forward(req, resp);
                    break;
                case "/orders":
                    req.setAttribute("orders", orders.buyerHistory(user.getId()));
                    req.getRequestDispatcher("/WEB-INF/jsp/orders.jsp").forward(req, resp);
                    break;
                default:
                    req.getRequestDispatcher("/WEB-INF/jsp/review.jsp").forward(req, resp);
            }
        } catch (AppException e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/cart.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        CartService cart = (CartService) getServletContext().getAttribute(DataSourceListener.CART);
        OrderService orders = (OrderService) getServletContext().getAttribute(DataSourceListener.ORDERS);
        ReviewService reviews = (ReviewService) getServletContext().getAttribute(DataSourceListener.REVIEWS);
        String path = req.getServletPath();
        try {
            if ("/cart".equals(path)) {
                String action = req.getParameter("action");
                if ("add".equals(action)) {
                    cart.add(user.getId(), Long.parseLong(req.getParameter("productId")),
                            Integer.parseInt(req.getParameter("quantity")));
                } else if ("update".equals(action)) {
                    cart.update(user.getId(), Long.parseLong(req.getParameter("id")),
                            Integer.parseInt(req.getParameter("quantity")));
                } else if ("remove".equals(action)) {
                    cart.remove(user.getId(), Long.parseLong(req.getParameter("id")));
                }
                resp.sendRedirect(req.getContextPath() + "/cart");
                return;
            }
            if ("/checkout".equals(path)) {
                CheckoutRequest checkout = new CheckoutRequest.Builder()
                        .confirmPayment("on".equals(req.getParameter("confirmPayment"))
                                || "true".equals(req.getParameter("confirmPayment")))
                        .build();
                orders.checkout(user.getId(), checkout);
                resp.sendRedirect(req.getContextPath() + "/orders");
                return;
            }
            if ("/review".equals(path)) {
                reviews.add(user.getId(), Long.parseLong(req.getParameter("productId")),
                        Long.parseLong(req.getParameter("orderId")),
                        Integer.parseInt(req.getParameter("rating")),
                        req.getParameter("comment"));
                resp.sendRedirect(req.getContextPath() + "/product?id=" + req.getParameter("productId"));
                return;
            }
        } catch (AppException e) {
            req.setAttribute("error", e.getMessage());
            doGet(req, resp);
        }
    }
}
