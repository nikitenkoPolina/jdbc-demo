package ru.jdbc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class ConnectionManager {

    static final Logger LOG = LoggerFactory.getLogger(ConnectionManager.class);

    private static final String LOGIN_KEY = "db.login";
    private static final String PASSWORD_KEY = "db.password";
    private static final String URL_KEY= "db.url";
    private static final String POOL_SIZE_KEY = "db.pool.size";
    private static final Integer DEFAULT_POOL_SIZE_KEY = 10;
    private static BlockingQueue<Connection> pool;

    // Статический блок инициализации
    static {
        loadDriver();
        initConnectionPool();
    }

    private ConnectionManager() {}

    private static void initConnectionPool() {
        var poolSize = PropertiesUtils.getProperty(POOL_SIZE_KEY);

        var size = poolSize == null ? DEFAULT_POOL_SIZE_KEY : Integer.parseInt(poolSize);
        pool = new ArrayBlockingQueue<>(size);

        for (int i = 0; i < size; i++) {
            var connection = open();
            var proxyConnection = (Connection)
                    Proxy.newProxyInstance(ConnectionManager.class.getClassLoader(), new Class[]{Connection.class},
                    (proxy, method, args) -> method.getName().equals("close")
                            ? pool.add((Connection) proxy)
                            : method.invoke(connection, args));
            pool.add(proxyConnection);
        }
    }

    public static Connection get() {
        try {
            return pool.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static Connection open() {
        try {
            LOG.info("Подключение к БД");
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
