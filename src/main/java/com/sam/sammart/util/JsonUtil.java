package com.sam.sammart.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sam.sammart.dto.ApiResponse;
import com.sam.sammart.exception.AppException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JSON envelope writer for versioned API endpoints.
 */
public final class JsonUtil {
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private JsonUtil() {
    }

    public static Gson gson() {
        return GSON;
    }

    public static <T> T fromJson(String body, Class<T> type) {
        return GSON.fromJson(body, type);
    }

    public static void writeOk(HttpServletResponse resp, int status, Object data) throws IOException {
        write(resp, status, ApiResponse.ok(data));
    }

    public static void writeError(HttpServletResponse resp, AppException ex) throws IOException {
        write(resp, ex.getStatus(), ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    public static void writeError(HttpServletResponse resp, int status, String code, String message) throws IOException {
        write(resp, status, ApiResponse.error(code, message));
    }

    public static JsonObject asObject(String body) {
        return GSON.fromJson(body, JsonObject.class);
    }

    private static void write(HttpServletResponse resp, int status, Object payload) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(GSON.toJson(payload));
    }
}
