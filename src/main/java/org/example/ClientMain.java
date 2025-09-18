package org.example;

import org.example.client.gui.dialogs.LoginDialog;
import org.example.client.gui.MainWindow;
import org.example.client.network.GuiClient;

import javax.swing.*;

public class ClientMain {
    public static void main(String[] args) {
        // Устанавливаем Look and Feel для лучшего внешнего вида
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error setting look and feel: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            // Парсим аргументы командной строки
            String host = "localhost";
            int port = 5432;

            if (args.length > 0) {
                host = args[0];
            }
            if (args.length > 1) {
                try {
                    port = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid port format. Using default port: " + port);
                }
            }

            // Подключаемся к серверу
            GuiClient client = GuiClient.getInstance();
            boolean connected = client.connect(host, port);

            if (connected) {
                // Показываем диалог авторизации
                boolean loggedIn = LoginDialog.showLoginDialog(null);

                if (loggedIn) {
                    // Показываем главное окно
                    MainWindow.showMainWindow();
                } else {
                    System.exit(0);
                }
            } else {
                JOptionPane.showMessageDialog(null,
                        "Failed to connect to server. Please make sure the server is running.",
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}