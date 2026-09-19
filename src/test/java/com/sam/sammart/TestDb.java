package com.sam.sammart;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

final class TestDb {
    private TestDb() {
    }

    static HikariDataSource memory() throws Exception {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl("jdbc:h2:mem:sammart_" + java.util.UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;MODE=REGULAR");
        cfg.setUsername("sa");
        cfg.setPassword("");
        cfg.setDriverClassName("org.h2.Driver");
        HikariDataSource ds = new HikariDataSource(cfg);
        applySchema(ds);
        return ds;
    }

    static void applySchema(DataSource ds) throws Exception {
        String sql;
        try (InputStream in = TestDb.class.getResourceAsStream("/schema.sql");
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            sql = reader.lines().collect(Collectors.joining("\n"));
        }
        try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {
            for (String part : sql.split(";")) {
                String stmt = part.trim();
                if (!stmt.isEmpty()) {
                    st.execute(stmt);
                }
            }
        }
    }
}
