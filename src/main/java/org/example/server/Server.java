package org.example.server;

import org.example.management.CollectionManager;
import org.example.management.CommandManager;
import org.example.management.DatabaseManager;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {
    private final int port;
    private final CollectionManager collectionManager;
    private final CommandManager commandManager;
    private final DatabaseManager databaseManager;
    private final ForkJoinPool forkJoinPool;
    private final AtomicBoolean isRunning;
    private ServerSocket serverSocket;

    public Server(int port, CollectionManager collectionManager, CommandManager commandManager, DatabaseManager databaseManager) {
        this.port = port;
        this.collectionManager = collectionManager;
        this.commandManager = commandManager;
        this.databaseManager = databaseManager;
        this.forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        this.isRunning = new AtomicBoolean(false);
    }

    public void start() {
        if (isRunning.get()) {
            System.out.println("Сервер уже запущен");
            return;
        }
        isRunning.set(true);

        try {
            // Загрузка данных из БД при запуске сервера
            collectionManager.loadFromDatabase();
            System.out.println("Коллекция загружена из БД. Элементов: " + collectionManager.getCollection().size());
            serverSocket = new ServerSocket(port);
            System.out.println("Сервер запущен на порту " + port);

            while (isRunning.get()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Подключен новый клиент: " + clientSocket.getInetAddress());
                    // Создаем новый поток для обработки клиента
                    new ClientHandler(clientSocket, collectionManager, commandManager, databaseManager, forkJoinPool).start();
                } catch (Exception e) {
                    if (isRunning.get()) {
                        System.err.println("Ошибка при принятии подключения: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка запуска сервера: " + e.getMessage());
        } finally {
            stop();
        }
    }

    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
                forkJoinPool.shutdown();
                System.out.println("Сервер остановлен");
            } catch (Exception e) {
                System.err.println("Ошибка при остановке сервера: " + e.getMessage());
            }
        }
    }
}