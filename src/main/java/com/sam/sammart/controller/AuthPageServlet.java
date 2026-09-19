package com.sam.sammart.controller;

import com.sam.sammart.exception.AppException;
import com.sam.sammart.listener.DataSourceListener;
import com.sam.sammart.model.User;
import com.sam.sammart.service.AuthService;
import com.sam.sammart.service.ProductService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(urlPatterns = {"/login", "/register", "/logout"})
public class AuthPageServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/logout".equals(path)) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/jsp/" + path.substring(1) + ".jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AuthService auth = (AuthService) getServletContext().getAttribute(DataSourceListener.AUTH);
        String path = req.getServletPath();
        try {
            if ("/register".equals(path)) {
                auth.register(req.getParameter("name"), req.getParameter("email"),
                        req.getParameter("password"), req.getParameter("role"));
                resp.sendRedirect(req.getContextPath() + "/login?registered=1");
                return;
            }
            User user = auth.login(req.getParameter("email"), req.getParameter("password"));
            HttpSession old = req.getSession(false);
            if (old != null) {
                old.invalidate();
            }
            HttpSession session = req.getSession(true);
            session.setAttribute("user", user);
            session.setMaxInactiveInterval(30 * 60);
            resp.sendRedirect(req.getContextPath() + "/");
        } catch (AppException e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp" + path + ".jsp").forward(req, resp);
        }
    }

    static void loadCatalog(HttpServletRequest req) throws AppException {
        ProductService products = (ProductService) req.getServletContext().getAttribute(DataSourceListener.PRODUCTS);
        Long cat = null;
        if (req.getParameter("categoryId") != null && !req.getParameter("categoryId").isBlank()) {
            cat = Long.parseLong(req.getParameter("categoryId"));
        }
        req.setAttribute("products", products.browse(req.getParameter("q"), cat));
        req.setAttribute("categories", products.categories());
    }
}
