package com.sam.sammart.controller;

import com.sam.sammart.exception.AppException;
import com.sam.sammart.listener.DataSourceListener;
import com.sam.sammart.model.User;
import com.sam.sammart.service.OrderService;
import com.sam.sammart.service.ProductService;
import com.sam.sammart.service.ReviewService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/*")
public class AdminServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ReviewService reviews = (ReviewService) getServletContext().getAttribute(DataSourceListener.REVIEWS);
        OrderService orders = (OrderService) getServletContext().getAttribute(DataSourceListener.ORDERS);
        ProductService products = (ProductService) getServletContext().getAttribute(DataSourceListener.PRODUCTS);
        try {
            req.setAttribute("users", reviews.allUsers());
            req.setAttribute("orders", orders.allOrders());
            req.setAttribute("products", products.browse(null, null));
            req.getRequestDispatcher("/WEB-INF/jsp/admin.jsp").forward(req, resp);
        } catch (AppException e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/admin.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        ProductService products = (ProductService) getServletContext().getAttribute(DataSourceListener.PRODUCTS);
        OrderService orders = (OrderService) getServletContext().getAttribute(DataSourceListener.ORDERS);
        try {
            if ("remove".equals(req.getParameter("action"))) {
                products.adminRemove(Long.parseLong(req.getParameter("id")));
            } else if ("status".equals(req.getParameter("action"))) {
                orders.changeStatus(user.getId(), user.getRole(), Long.parseLong(req.getParameter("orderId")),
                        req.getParameter("status"));
            }
            resp.sendRedirect(req.getContextPath() + "/admin/");
        } catch (AppException e) {
            req.setAttribute("error", e.getMessage());
            doGet(req, resp);
        }
    }
}
