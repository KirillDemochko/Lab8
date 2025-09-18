package org.example.util;

import org.example.management.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SSHTunnelChecker {
    public static void main(String[] args) {
        // 1. Проверка SSH-туннеля
        System.out.println("Проверка SSH-туннеля...");
        checkSSHTunnel();

        // 2. Проверка пула соединений
        System.out.println("\nПроверка HikariCP...");
        testConnectionPool();
    }

    private static void checkSSHTunnel() {
        try {
            Process process = Runtime.getRuntime().exec("ps aux | grep ssh");
            process.waitFor();

            String output = new String(process.getInputStream().readAllBytes());
            if (output.contains("5432:pg:5432")) {
                System.out.println("✅ SSH-туннель активен");
            } else {
                System.out.println("❌ SSH-туннель не найден. Запустите:");
                System.out.println("ssh -p 2222 sXXXXXX@helios.cs.ifmo.ru -L 5432:pg:5432 -N");
            }
        } catch (Exception e) {
            System.err.println("Ошибка проверки SSH: " + e.getMessage());
        }
    }

    private static void testConnectionPool() {
        try {
            DatabaseConnection.logPoolStatus();

            try (Connection conn = DatabaseConnection.getConnection()) {
                System.out.println("✔ Получено соединение из пула");
                DatabaseConnection.logPoolStatus();

                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT version()")) {
                    if (rs.next()) {
                        System.out.println("Версия PostgreSQL: " + rs.getString(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка подключения: " + e.getMessage());
            System.err.println("Проверьте: " +
                    "1. Активность SSH-туннеля\n" +
                    "2. Параметры подключения в DatabaseConnection\n" +
                    "3. Доступность БД через psql -h localhost -U ваш_логин -d studs");
        } finally {
            DatabaseConnection.close();
        }
    }
}