package org.example.management;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class DatabaseConnection {
    private static final HikariDataSource dataSource;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private static final String SSH_HOST = "helios.cs.ifmo.ru";
    private static final int SSH_PORT = 2222;
    private static final String SSH_USER = "s465751";
    private static final String SSH_PASSWORD = "YKmD%3472";

    private static final String DB_REMOTE_HOST = "pg";
    private static final int DB_REMOTE_PORT = 5432;
    private static final int LOCAL_FORWARDED_PORT = 5433;
    private static final String DB_USER = "s465751";
    private static final String DB_NAME = "studs";
    private static final String DB_PASSWORD = "BiX8U9qeW7Kg8RhO";

    private static Session sshSession;

    static {
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("PostgreSQL JDBC Driver успешно зарегистрирован");

            establishSshTunnel();

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:postgresql://localhost:" + LOCAL_FORWARDED_PORT + "/" + DB_NAME);
            config.setUsername(DB_USER);
            config.setPassword(DB_PASSWORD);

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(TimeUnit.SECONDS.toMillis(10));
            config.setIdleTimeout(TimeUnit.MINUTES.toMillis(5));
            config.setMaxLifetime(TimeUnit.MINUTES.toMillis(30));
            config.setLeakDetectionThreshold(TimeUnit.SECONDS.toMillis(10));
            config.addDataSourceProperty("socketTimeout", "30");
            config.addDataSourceProperty("tcpKeepAlive", "true");
            config.addDataSourceProperty("prepareThreshold", "0");
            config.setValidationTimeout(TimeUnit.SECONDS.toMillis(5));
            config.setConnectionTestQuery("SELECT 1");
            config.setInitializationFailTimeout(TimeUnit.SECONDS.toMillis(30));

            dataSource = new HikariDataSource(config);
            testConnection();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver не найден. Проверьте зависимости Maven.", e);
        } catch (JSchException e) {
            throw new RuntimeException("Ошибка установки SSH-туннеля: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка инициализации пула соединений: " + e.getMessage(), e);
        }
    }

    private static void testConnection() throws SQLException {
        try (Connection conn = getConnectionWithRetry()) {
            if (!conn.isValid(5)) {
                throw new SQLException("Тестовое соединение невалидно");
            }
            System.out.println("[HikariCP] Пул соединений успешно инициализирован");
        }
    }

    public static Connection getConnection() throws SQLException {
        return getConnectionWithRetry();
    }

    private static Connection getConnectionWithRetry() throws SQLException {
        SQLException lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                Connection conn = dataSource.getConnection();
                System.out.printf("[HikariCP] Получено соединение (попытка %d/%d)%n", attempt, MAX_RETRY_ATTEMPTS);
                return conn;
            } catch (SQLException e) {
                lastException = e;
                System.err.printf("[HikariCP] Ошибка получения соединения (попытка %d/%d): %s%n",
                        attempt, MAX_RETRY_ATTEMPTS, e.getMessage());

                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("Прервано во время ожидания повторной попытки", ie);
                    }

                    // Проверяем статус SSH-туннеля и переподключаемся при необходимости
                    if (!isSshTunnelActive()) {
                        System.out.println("[SSH] Попытка восстановить SSH-туннель...");
                        try {
                            reestablishSshTunnel();
                        } catch (JSchException jse) {
                            System.err.println("[SSH] Ошибка восстановления туннеля: " + jse.getMessage());
                        }
                    }
                }
            }
        }

        throw new SQLException("Не удалось получить соединение после " + MAX_RETRY_ATTEMPTS +
                " попыток", lastException);
    }

    private static boolean isSshTunnelActive() {
        return sshSession != null && sshSession.isConnected();
    }

    private static void reestablishSshTunnel() throws JSchException {
        if (sshSession != null && sshSession.isConnected()) {
            sshSession.disconnect();
        }
        establishSshTunnel();
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("[HikariCP] Пул соединений закрыт");
        }
        if (sshSession != null && sshSession.isConnected()) {
            sshSession.disconnect();
            System.out.println("SSH-сессия закрыта");
        }
    }

    // Методы для мониторинга
    public static int getActiveConnections() {
        return dataSource.getHikariPoolMXBean().getActiveConnections();
    }

    public static void logPoolStatus() {
        System.out.printf(
                "[HikariCP] Статус: Active=%d, Idle=%d, Total=%d, Waiting=%d%n",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
        );
    }

    public static void establishSshTunnel() throws JSchException {
        JSch jsch = new JSch();
        sshSession = jsch.getSession(SSH_USER, SSH_HOST, SSH_PORT);
        sshSession.setPassword(SSH_PASSWORD);

        System.out.println("Используется SSH-аутентификация по паролю для пользователя " + SSH_USER);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        config.put("PreferredAuthentications", "publickey,keyboard-interactive,password");
        sshSession.setConfig(config);

        System.out.println("Установка SSH-соединения с " + SSH_HOST + ":" + SSH_PORT + "...");
        sshSession.connect(30000);

        if (!sshSession.isConnected()) {
            throw new JSchException("Не удалось установить SSH-соединение с " + SSH_HOST);
        }
        System.out.println("SSH-соединение с " + SSH_HOST + " установлено.");

        // Устанавливаем проброс локального порта
        int assignedPort = sshSession.setPortForwardingL(LOCAL_FORWARDED_PORT, DB_REMOTE_HOST, DB_REMOTE_PORT);
        System.out.println("Локальный порт " + assignedPort + " проброшен на " +
                DB_REMOTE_HOST + ":" + DB_REMOTE_PORT + " через SSH-туннель.");
    }
}