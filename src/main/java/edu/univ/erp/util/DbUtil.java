package edu.univ.erp.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DbUtil {

    private static final Properties PROPS = new Properties();
    private static boolean initialized = false;


    private static synchronized void init() {
        if (initialized) return;
        try (InputStream in = DbUtil.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in == null) {
                throw new RuntimeException("application.properties not found in classpath");
            }
            PROPS.load(in);
            initialized = true;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load DB properties", e);
        }
    }

    public static String get(String key) {
        if (!initialized) init();
        String value = PROPS.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Missing property: " + key);
        }
        return value;
    }

    public static Connection getAuthConnection() throws SQLException {
        String url = get("auth.jdbc.url");
        String user = get("auth.jdbc.user");
        String pass = get("auth.jdbc.password");
        // FIX: Should retrieve connection from a pool here instead of DriverManager
        return DriverManager.getConnection(url, user, pass);
    }

    public static Connection getErpConnection() throws SQLException {
        String url = get("erp.jdbc.url");
        String user = get("erp.jdbc.user");
        String pass = get("erp.jdbc.password");
        // FIX: Should retrieve connection from a pool here instead of DriverManager
        return DriverManager.getConnection(url, user, pass);
    }
}