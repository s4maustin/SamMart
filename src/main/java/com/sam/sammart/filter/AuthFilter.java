package com.sam.sammart.filter;

import com.sam.sammart.model.User;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter extends HttpFilter {
    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        String path = req.getRequestURI().substring(req.getContextPath().length());
        if (isPublic(path, req.getMethod())) {
            chain.doFilter(req, res);
            return;
        }
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) {
            if (path.startsWith("/api/")) {
                res.setStatus(401);
                res.setContentType("application/json");
                res.getWriter().write("{\"success\":false,\"data\":null,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"login required\"}}");
                return;
            }
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (path.startsWith("/seller") && !"SELLER".equals(user.getRole()) && !"ADMIN".equals(user.getRole())) {
            res.sendError(403);
            return;
        }
        if (path.startsWith("/admin") && !"ADMIN".equals(user.getRole())) {
            res.sendError(403);
            return;
        }
        if ((path.startsWith("/cart") || path.startsWith("/checkout") || path.equals("/orders")
                || path.equals("/review"))
                && !"BUYER".equals(user.getRole()) && !"ADMIN".equals(user.getRole())) {
            res.sendError(403);
            return;
        }
        req.setAttribute("authUser", user);
        chain.doFilter(req, res);
    }

    private boolean isPublic(String path, String method) {
        if (path.startsWith("/css/") || path.startsWith("/js/") || path.equals("/error.jsp")) {
            return true;
        }
        if (path.equals("/login") || path.equals("/register") || path.equals("/") || path.equals("/index.jsp")
                || path.equals("/home") || path.equals("/product") || path.startsWith("/api/v1/health")
                || path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/register")) {
            return true;
        }
        if ("GET".equals(method) && (path.equals("/api/v1/products") || path.startsWith("/api/v1/products/"))) {
            return true;
        }
        return false;
    }
}
