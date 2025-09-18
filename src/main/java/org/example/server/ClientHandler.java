package org.example.server;

import org.example.management.*;
import org.example.data.User;
import org.example.util.HashUtil;
import org.example.network.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

public class ClientHandler extends Thread {
    private final Socket clientSocket;
    private final CollectionManager collectionManager;
    private final CommandManager commandManager;
    private final DatabaseManager databaseManager;
    private final ForkJoinPool forkJoinPool;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private User currentUser;
    private static final ConcurrentHashMap<String, User> activeUsers = new ConcurrentHashMap<>();

    public ClientHandler(Socket socket, CollectionManager collectionManager,
                         CommandManager commandManager, DatabaseManager databaseManager,
                         ForkJoinPool forkJoinPool) {
        this.clientSocket = socket;
        this.collectionManager = collectionManager;
        this.commandManager = commandManager;
        this.databaseManager = databaseManager;
        this.forkJoinPool = forkJoinPool;
    }

    @Override
    public void run() {
        try {
            // Создаем потоки ввода-вывода
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            inputStream = new ObjectInputStream(clientSocket.getInputStream());

            System.out.println("Обработчик клиента запущен: " + clientSocket.getInetAddress());

            // Основной цикл обработки запросов
            while (!clientSocket.isClosed() && clientSocket.isConnected()) {
                try {
                    Object request = inputStream.readObject();

                    if (request instanceof AuthRequest) {
                        handleAuthRequest((AuthRequest) request);
                    } else if (request instanceof CommandRequest) {
                        handleCommandRequest((CommandRequest) request);
                    } else if (request instanceof String && "GET_PRODUCTS".equals(request)) {
                        handleGetProductsRequest();
                    } else {
                        sendResponse(new Response(false, "Неизвестный тип запроса"));
                    }
                } catch (ClassNotFoundException e) {
                    sendResponse(new Response(false, "Ошибка десериализации: " + e.getMessage()));
                } catch (IOException e) {
                    if (!clientSocket.isClosed()) {
                        System.err.println("Ошибка чтения запроса: " + e.getMessage());
                    }
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка инициализации потоков: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void handleAuthRequest(AuthRequest authRequest) {
        try {
            String username = authRequest.getUsername();
            String password = authRequest.getPassword();

            if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
                sendResponse(new Response(false, "Логин и пароль не могут быть пустыми"));
                return;
            }

            // Обработка регистрации
            if (authRequest.isRegistration()) {
                // Проверяем существование пользователя
                User existingUser = databaseManager.getUserByUsername(username);
                if (existingUser != null) {
                    sendResponse(new Response(false, "Пользователь с таким логином уже существует"));
                    return;
                }

                // Регистрируем нового пользователя
                User newUser = databaseManager.registerUser(username, HashUtil.sha256(password));
                if (newUser != null) {
                    // АВТОМАТИЧЕСКИ АВТОРИЗУЕМ ПОЛЬЗОВАТЕЛЯ ПОСЛЕ РЕГИСТРАЦИИ
                    currentUser = newUser;
                    activeUsers.put(username, newUser);
                    sendResponse(new Response(true, "Регистрация успешна! Добро пожаловать, " + username, newUser.getId()));
                } else {
                    sendResponse(new Response(false, "Ошибка при регистрации пользователя"));
                }
                return;
            }

            // Обработка входа (существующая логика)
            User user = databaseManager.authenticateUser(username, HashUtil.sha256(password));
            if (user != null) {
                if (activeUsers.containsKey(username)) {
                    sendResponse(new Response(false, "Пользователь уже авторизован в системе"));
                    return;
                }

                currentUser = user;
                activeUsers.put(username, user);
                // ОТПРАВЛЯЕМ ID ПОЛЬЗОВАТЕЛЯ В data
                sendResponse(new Response(true, "Авторизация успешна. Добро пожаловать, " + username, user.getId()));
                System.out.println("Пользователь авторизован: " + username);
            } else {
                sendResponse(new Response(false, "Неверный логин или пароль"));
            }
        } catch (Exception e) {
            sendResponse(new Response(false, "Ошибка авторизации: " + e.getMessage()));
        }
    }

    private void handleCommandRequest(CommandRequest commandRequest) {
        // Проверяем авторизацию
        if (currentUser == null) {
            sendResponse(new Response(false, "Требуется авторизация"));
            return;
        }

        // Проверяем совпадение хэша пароля для безопасности
        if (!commandRequest.getPasswordHash().equals(currentUser.getPasswordHash())) {
            sendResponse(new Response(false, "Ошибка аутентификации"));
            return;
        }

        // Выполняем команду в пуле потоков
        forkJoinPool.execute(() -> {
            try {
                String commandName = commandRequest.getCommand();
                String[] args = commandRequest.getArgs();

                // Синхронизируем доступ к коллекции
                synchronized (collectionManager) {
                    String result = commandManager.executeCommand(commandName, args, currentUser);
                    sendResponse(new Response(true, result));
                }
            } catch (Exception e) {
                sendResponse(new Response(false, "Ошибка выполнения команды: " + e.getMessage()));
            }
        });
    }

    private void handleGetProductsRequest() {
        // Проверяем авторизацию
        if (currentUser == null) {
            sendResponse(new Response(false, "Требуется авторизация"));
            return;
        }

        try {
            // Отправляем список продуктов напрямую
            synchronized (collectionManager) {
                outputStream.writeObject(collectionManager.getCollection());
                outputStream.flush();
            }
        } catch (IOException e) {
            System.err.println("Ошибка отправки списка продуктов: " + e.getMessage());
        }
    }

    private void sendResponse(Response response) {
        try {
            synchronized (outputStream) {
                outputStream.writeObject(response);
                outputStream.flush();
            }
        } catch (IOException e) {
            System.err.println("Ошибка отправки ответа: " + e.getMessage());
        }
    }

    private void closeConnection() {
        // Удаляем пользователя из списка активных
        if (currentUser != null) {
            activeUsers.remove(currentUser.getUsername());
            System.out.println("Пользователь отключен: " + currentUser.getUsername());
        }

        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии соединения: " + e.getMessage());
        }

        System.out.println("Клиент отключен: " + clientSocket.getInetAddress());
    }

    public static boolean isUserActive(String username) {
        return activeUsers.containsKey(username);
    }
}