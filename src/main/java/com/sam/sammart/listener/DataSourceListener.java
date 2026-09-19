package com.sam.sammart.listener;

import com.sam.sammart.dao.DaoFactory;
import com.sam.sammart.model.Product;
import com.sam.sammart.model.User;
import com.sam.sammart.service.AuthService;
import com.sam.sammart.service.CartService;
import com.sam.sammart.service.MockPaymentChannel;
import com.sam.sammart.service.OrderService;
import com.sam.sammart.service.ProductService;
import com.sam.sammart.service.ReviewService;
import com.sam.sammart.util.PasswordUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Singleton connection-pool lifecycle. The only place DriverManager/Hikari is constructed.
 */
@WebListener
public class DataSourceListener implements ServletContextListener {
    public static final String DS = "sammart.datasource";
    public static final String AUTH = "sammart.authService";
    public static final String PRODUCTS = "sammart.productService";
    public static final String CART = "sammart.cartService";
    public static final String ORDERS = "sammart.orderService";
    public static final String REVIEWS = "sammart.reviewService";
    public static final String DAO = "sammart.daoFactory";

    private static final Logger LOG = LoggerFactory.getLogger(DataSourceListener.class);
    private HikariDataSource pool;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Properties props = loadProps();
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(props.getProperty("jdbc.url"));
        cfg.setUsername(props.getProperty("jdbc.username", "sa"));
        cfg.setPassword(props.getProperty("jdbc.password", ""));
        cfg.setDriverClassName("org.h2.Driver");
        cfg.setMaximumPoolSize(Integer.parseInt(props.getProperty("hikari.maximumPoolSize", "10")));
        cfg.setPoolName("SamMartPool");
        pool = new HikariDataSource(cfg);
        applySchema(pool);
        DaoFactory factory = new DaoFactory(pool);
        seed(factory);
        ServletContext ctx = sce.getServletContext();
        ctx.setAttribute(DS, pool);
        ctx.setAttribute(DAO, factory);
        ctx.setAttribute(AUTH, new AuthService(factory.userDao()));
        ProductService products = new ProductService(factory.productDao());
        ctx.setAttribute(PRODUCTS, products);
        ctx.setAttribute(CART, new CartService(factory.cartDao(), factory.productDao()));
        ctx.setAttribute(ORDERS, new OrderService(pool, factory.cartDao(), factory.productDao(), factory.orderDao(),
                factory.supportDao(), new MockPaymentChannel()));
        ctx.setAttribute(REVIEWS, new ReviewService(factory.reviewDao(), factory.orderDao(), factory.productDao(),
                factory.userDao()));
        LOG.info("SamMart datasource initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (pool != null) {
            pool.close();
        }
    }

    public static DataSource dataSource(ServletContext ctx) {
        return (DataSource) ctx.getAttribute(DS);
    }

    private Properties loadProps() {
        Properties props = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load config.properties", e);
        }
        if (!props.containsKey("jdbc.url")) {
            props.setProperty("jdbc.url", "jdbc:h2:mem:sammart;DB_CLOSE_DELAY=-1");
        }
        return props;
    }

    private void applySchema(DataSource ds) {
        try (Connection c = ds.getConnection(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME IN ('USERS','users')")) {
            rs.next();
            if (rs.getInt(1) > 0) {
                return;
            }
        } catch (Exception ignored) {
            // first boot
        }
        String sql = readResource("/db/schema.sql");
        try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {
            for (String part : sql.split(";")) {
                String stmt = part.trim();
                if (!stmt.isEmpty()) {
                    st.execute(stmt);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Schema apply failed", e);
        }
    }

    private void seed(DaoFactory factory) {
        try {
            if (factory.userDao().emailExists("admin@sammart.local")) {
                return;
            }
            User admin = new User();
            admin.setName("Sam Admin");
            admin.setEmail("admin@sammart.local");
            admin.setPasswordHash(PasswordUtil.hash("Admin@123"));
            admin.setRole("ADMIN");
            factory.userDao().insert(admin);

            User seller = new User();
            seller.setName("Priya Seller");
            seller.setEmail("seller@sammart.local");
            seller.setPasswordHash(PasswordUtil.hash("Seller@123"));
            seller.setRole("SELLER");
            factory.userDao().insert(seller);

            User buyer = new User();
            buyer.setName("Arun Buyer");
            buyer.setEmail("buyer@sammart.local");
            buyer.setPasswordHash(PasswordUtil.hash("Buyer@123"));
            buyer.setRole("BUYER");
            factory.userDao().insert(buyer);

            String[][] cats = {
                    {"Electronics", "electronics"}, {"Books", "books"}, {"Home", "home"},
                    {"Fashion", "fashion"}, {"Sports", "sports"}
            };
            for (String[] cat : cats) {
                factory.productDao().insertCategory(cat[0], cat[1]);
            }
            insertDemo(factory, seller.getId(), 1, "Wireless Mouse", "Ergonomic 2.4GHz mouse with USB receiver.",
                    "799.00", 40);
            insertDemo(factory, seller.getId(), 1, "USB-C Hub", "7-in-1 hub for laptops: HDMI, USB-A, SD.",
                    "1499.00", 25);
            insertDemo(factory, seller.getId(), 2, "Clean Code", "Paperback on writing maintainable software.",
                    "599.00", 15);
            insertDemo(factory, seller.getId(), 3, "Desk Lamp", "LED lamp with three brightness levels.",
                    "899.00", 18);
            insertDemo(factory, seller.getId(), 5, "Yoga Mat", "6mm non-slip mat for home workouts.",
                    "499.00", 30);
            factory.supportDao().insertAudit(admin.getId(), "SEED", "system", null, "demo users and catalog");
        } catch (Exception e) {
            throw new IllegalStateException("Seed failed", e);
        }
    }

    private void insertDemo(DaoFactory factory, long sellerId, long categoryId, String name, String desc,
                            String price, int stock) throws Exception {
        Product p = new Product();
        p.setSellerId(sellerId);
        p.setCategoryId(categoryId);
        p.setName(name);
        p.setDescription(desc);
        p.setPrice(new BigDecimal(price));
        p.setStockQty(stock);
        p.setImageUrl("https://picsum.photos/seed/" + name.replace(" ", "") + "/400/300");
        factory.productDao().insert(p);
    }

    private String readResource(String path) {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Missing " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
