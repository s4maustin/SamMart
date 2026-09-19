package com.sam.sammart.controller;

import com.sam.sammart.exception.AppException;
import com.sam.sammart.listener.DataSourceListener;
import com.sam.sammart.service.ProductService;
import com.sam.sammart.service.ReviewService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/home", "/product"})
public class CatalogServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if ("/product".equals(req.getServletPath())) {
                ProductService products = (ProductService) getServletContext().getAttribute(DataSourceListener.PRODUCTS);
                ReviewService reviews = (ReviewService) getServletContext().getAttribute(DataSourceListener.REVIEWS);
                long id = Long.parseLong(req.getParameter("id"));
                req.setAttribute("product", products.get(id));
                req.setAttribute("reviews", reviews.forProduct(id));
                req.getRequestDispatcher("/WEB-INF/jsp/product.jsp").forward(req, resp);
                return;
            }
            AuthPageServlet.loadCatalog(req);
            req.getRequestDispatcher("/WEB-INF/jsp/index.jsp").forward(req, resp);
        } catch (AppException e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/index.jsp").forward(req, resp);
        }
    }
}
