package com.sam.sammart.controller;

import com.sam.sammart.exception.AppException;
import com.sam.sammart.listener.DataSourceListener;
import com.sam.sammart.model.User;
import com.sam.sammart.service.OrderService;
import com.sam.sammart.service.ProductService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/seller/*")
public class SellerServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        ProductService products = (ProductService) getServletContext().getAttribute(DataSourceListener.PRODUCTS);
        OrderService orders = (OrderService) getServletContext().getAttribute(DataSourceListener.ORDERS);
        try {
            req.setAttribute("products", products.sellerListings(user.getId()));
            req.setAttribute("categories", products.categories());
            req.setAttribute("incoming", orders.sellerIncoming(user.getId()));
            req.getRequestDispatcher("/WEB-INF/jsp/seller.jsp").forward(req, resp);
        } catch (AppException e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/seller.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        ProductService products = (ProductService) getServletContext().getAttribute(DataSourceListener.PRODUCTS);
        OrderService orders = (OrderService) getServletContext().getAttribute(DataSourceListener.ORDERS);
        String action = req.getParameter("action");
        try {
            if ("create".equals(action)) {
                products.create(user.getId(), Long.parseLong(req.getParameter("categoryId")),
                        req.getParameter("name"), req.getParameter("description"),
                        new BigDecimal(req.getParameter("price")),
                        Integer.parseInt(req.getParameter("stockQty")),
                        req.getParameter("imageUrl"));
            } else if ("update".equals(action)) {
                products.update(user.getId(), Long.parseLong(req.getParameter("id")),
                        Long.parseLong(req.getParameter("categoryId")),
                        req.getParameter("name"), req.getParameter("description"),
                        new BigDecimal(req.getParameter("price")),
                        Integer.parseInt(req.getParameter("stockQty")),
                        req.getParameter("imageUrl"));
            } else if ("delete".equals(action)) {
                products.deleteOwn(user.getId(), Long.parseLong(req.getParameter("id")));
            } else if ("status".equals(action)) {
                orders.changeStatus(user.getId(), user.getRole(), Long.parseLong(req.getParameter("orderId")),
                        req.getParameter("status"));
            }
            resp.sendRedirect(req.getContextPath() + "/seller/");
        } catch (AppException e) {
            req.setAttribute("error", e.getMessage());
            doGet(req, resp);
        }
    }
}
