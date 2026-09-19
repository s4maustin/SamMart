package com.sam.sammart.controller;

import com.sam.sammart.dao.DaoFactory;
import com.sam.sammart.dto.CheckoutRequest;
import com.sam.sammart.exception.AppException;
import com.sam.sammart.listener.DataSourceListener;
import com.sam.sammart.model.User;
import com.sam.sammart.service.AuthService;
import com.sam.sammart.service.CartService;
import com.sam.sammart.service.OrderService;
import com.sam.sammart.service.ProductService;
import com.sam.sammart.service.ReviewService;
import com.sam.sammart.util.JsonUtil;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Front controller for versioned JSON APIs under /api/v1/*.
 */
@WebServlet("/api/v1/*")
public class ApiServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            dispatch(req, resp);
        } catch (AppException e) {
            JsonUtil.writeError(resp, e);
        } catch (Exception e) {
            getServletContext().log("API failure", e);
            JsonUtil.writeError(resp, 500, "SERVER_ERROR", "unexpected error");
        }
    }

    private void dispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String path = req.getPathInfo() == null ? "/" : req.getPathInfo();
        String method = req.getMethod();
        AuthService auth = (AuthService) getServletContext().getAttribute(DataSourceListener.AUTH);
        ProductService products = (ProductService) getServletContext().getAttribute(DataSourceListener.PRODUCTS);
        CartService cart = (CartService) getServletContext().getAttribute(DataSourceListener.CART);
        OrderService orders = (OrderService) getServletContext().getAttribute(DataSourceListener.ORDERS);
        ReviewService reviews = (ReviewService) getServletContext().getAttribute(DataSourceListener.REVIEWS);
        DaoFactory dao = (DaoFactory) getServletContext().getAttribute(DataSourceListener.DAO);
        User user = current(req);

        if ("/health".equals(path) && "GET".equals(method)) {
            boolean db = dao.supportDao().ping();
            JsonUtil.writeOk(resp, 200, Map.of("status", "UP", "db", db ? "UP" : "DOWN"));
            return;
        }
        if ("/auth/register".equals(path) && "POST".equals(method)) {
            JsonObject body = body(req);
            JsonUtil.writeOk(resp, 201, auth.register(str(body, "name"), str(body, "email"),
                    str(body, "password"), str(body, "role")));
            return;
        }
        if ("/auth/login".equals(path) && "POST".equals(method)) {
            JsonObject body = body(req);
            User logged = auth.login(str(body, "email"), str(body, "password"));
            HttpSession old = req.getSession(false);
            if (old != null) {
                old.invalidate();
            }
            HttpSession session = req.getSession(true);
            session.setAttribute("user", logged);
            session.setMaxInactiveInterval(30 * 60);
            JsonUtil.writeOk(resp, 200, auth.toDto(logged));
            return;
        }
        if ("/auth/logout".equals(path) && "POST".equals(method)) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            JsonUtil.writeOk(resp, 200, Map.of("loggedOut", true));
            return;
        }
        if ("/products".equals(path) && "GET".equals(method)) {
            Long cat = req.getParameter("categoryId") == null || req.getParameter("categoryId").isBlank()
                    ? null : Long.parseLong(req.getParameter("categoryId"));
            JsonUtil.writeOk(resp, 200, products.browse(req.getParameter("q"), cat));
            return;
        }
        if (path.startsWith("/products/") && "GET".equals(method)) {
            JsonUtil.writeOk(resp, 200, products.get(idAfter(path, "/products/")));
            return;
        }
        if ("/products".equals(path) && "POST".equals(method)) {
            requireRole(user, "SELLER");
            JsonObject body = body(req);
            JsonUtil.writeOk(resp, 201, products.create(user.getId(), body.get("categoryId").getAsLong(),
                    str(body, "name"), str(body, "description"), body.get("price").getAsBigDecimal(),
                    body.get("stockQty").getAsInt(), str(body, "imageUrl")));
            return;
        }
        if ("/cart".equals(path) && "GET".equals(method)) {
            requireUser(user);
            JsonUtil.writeOk(resp, 200, Map.of("items", cart.list(user.getId()), "total", cart.total(cart.list(user.getId()))));
            return;
        }
        if ("/cart".equals(path) && "POST".equals(method)) {
            requireRole(user, "BUYER");
            JsonObject body = body(req);
            cart.add(user.getId(), body.get("productId").getAsLong(), body.get("quantity").getAsInt());
            JsonUtil.writeOk(resp, 201, Map.of("added", true));
            return;
        }
        if ("/orders".equals(path) && "POST".equals(method)) {
            requireRole(user, "BUYER");
            JsonObject body = body(req);
            boolean confirm = body.has("confirmPayment") && body.get("confirmPayment").getAsBoolean();
            CheckoutRequest checkout = new CheckoutRequest.Builder().confirmPayment(confirm).build();
            JsonUtil.writeOk(resp, 201, orders.checkout(user.getId(), checkout));
            return;
        }
        if ("/orders".equals(path) && "GET".equals(method)) {
            requireUser(user);
            if ("BUYER".equals(user.getRole())) {
                JsonUtil.writeOk(resp, 200, orders.buyerHistory(user.getId()));
            } else if ("SELLER".equals(user.getRole())) {
                JsonUtil.writeOk(resp, 200, orders.sellerIncoming(user.getId()));
            } else {
                JsonUtil.writeOk(resp, 200, orders.allOrders());
            }
            return;
        }
        if ("/reviews".equals(path) && "POST".equals(method)) {
            requireRole(user, "BUYER");
            JsonObject body = body(req);
            reviews.add(user.getId(), body.get("productId").getAsLong(), body.get("orderId").getAsLong(),
                    body.get("rating").getAsInt(), str(body, "comment"));
            JsonUtil.writeOk(resp, 201, Map.of("reviewed", true));
            return;
        }
        if ("/admin/users".equals(path) && "GET".equals(method)) {
            requireRole(user, "ADMIN");
            JsonUtil.writeOk(resp, 200, reviews.allUsers().stream().map(auth::toDto).collect(Collectors.toList()));
            return;
        }
        JsonUtil.writeError(resp, 404, "NOT_FOUND", "unknown endpoint");
    }

    private User current(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (User) session.getAttribute("user");
    }

    private void requireUser(User user) throws AppException {
        if (user == null) {
            throw new com.sam.sammart.exception.UnauthorizedException("login required");
        }
    }

    private void requireRole(User user, String role) throws AppException {
        requireUser(user);
        if (!role.equals(user.getRole()) && !"ADMIN".equals(user.getRole())) {
            throw new com.sam.sammart.exception.ForbiddenException("role " + role + " required");
        }
    }

    private JsonObject body(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return JsonUtil.asObject(sb.toString());
    }

    private String str(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
    }

    private long idAfter(String path, String prefix) {
        return Long.parseLong(path.substring(prefix.length()).split("/")[0]);
    }
}
