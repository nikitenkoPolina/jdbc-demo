package ru.jdbc.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionManager {
    private static final String LOGIN_KEY = "db.login";
    private static final String PASSWORD_KEY = "db.password";
    private static final String URL_KEY= "db.url";

    private ConnectionManager() {}

    // Статический блок инициализации
    static {
        loadDriver();
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(
                    PropertiesUtils.getProperty(URL_KEY),
                    PropertiesUtils.getProperty(LOGIN_KEY),
                    PropertiesUtils.getProperty(PASSWORD_KEY)
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод для загрузки Postgres Driver
     * Для старых версий Java
     */
    private static void loadDriver() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
