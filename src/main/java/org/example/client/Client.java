package org.example.client;

import org.example.network.AuthRequest;
import org.example.network.CommandRequest;
import org.example.network.Response;
import org.example.util.HashUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private String host;
    private int port;
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private String username;
    private String passwordHash;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try {
            // Устанавливаем соединение с сервером
            socket = new Socket(host, port);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

            System.out.println("Подключено к серверу " + host + ":" + port);

            // Процесс аутентификации ИЛИ регистрации
            if (showAuthMenu()) {
                // Запускаем поток для чтения ответов от сервера
                new Thread(this::readResponses).start();

                // Основной цикл для отправки команд
                sendCommands();
            }

        } catch (IOException e) {
            System.err.println("Ошибка подключения: " + e.getMessage());
        } finally {
            close();
        }
    }

    private boolean showAuthMenu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== Меню ===");
            System.out.println("1. Войти");
            System.out.println("2. Зарегистрироваться");
            System.out.println("3. Выйти");
            System.out.print("Выберите действие: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    return authenticate(scanner);
                case "2":
                    return register(scanner);
                case "3":
                    System.out.println("До свидания!");
                    return false;
                default:
                    System.out.println("Неверный выбор. Попробуйте снова.");
            }
        }
    }

    private boolean register(Scanner scanner) {
        try {
            System.out.println("\n=== Регистрация ===");
            System.out.print("Придумайте логин: ");
            String newUsername = scanner.nextLine().trim();
            System.out.print("Придумайте пароль: ");
            String newPassword = scanner.nextLine().trim();

            if (newUsername.isEmpty() || newPassword.isEmpty()) {
                System.out.println("Ошибка: логин и пароль не могут быть пустыми.");
                return false;
            }

            // Создаем запрос регистрации
            AuthRequest registerRequest = new AuthRequest(newUsername, newPassword);
            registerRequest.setIsRegistration(true);

            outputStream.writeObject(registerRequest);
            outputStream.flush();

            // Получаем ответ от сервера
            Response response = (Response) inputStream.readObject();
            if (response.isSuccess()) {
                System.out.println(response.getMessage());
                // После успешной регистрации автоматически входим
                System.out.println("Автоматический вход...");
                this.username = newUsername;
                this.passwordHash = HashUtil.sha256(newPassword);
                return true;
            } else {
                System.err.println("Ошибка регистрации: " + response.getMessage());
                return false;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка при регистрации: " + e.getMessage());
            return false;
        }
    }

    private boolean authenticate(Scanner scanner) {
        try {
            System.out.println("\n=== Вход ===");
            System.out.print("Введите логин: ");
            username = scanner.nextLine().trim();
            System.out.print("Введите пароль: ");
            String password = scanner.nextLine().trim();
            passwordHash = HashUtil.sha256(password);

            // Отправляем запрос аутентификации
            AuthRequest authRequest = new AuthRequest(username, password);
            outputStream.writeObject(authRequest);
            outputStream.flush();

            // Получаем ответ от сервера
            Response response = (Response) inputStream.readObject();
            if (response.isSuccess()) {
                System.out.println(response.getMessage());
                return true;
            } else {
                System.err.println(response.getMessage());
                return false;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка аутентификации: " + e.getMessage());
            return false;
        }
    }

    private void sendCommands() {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("\nВведите команды (или 'exit' для выхода):");

            while (true) {
                String commandLine = scanner.nextLine();
                if ("exit".equalsIgnoreCase(commandLine)) {
                    break;
                }

                // Парсим команду и аргументы
                String[] parts = commandLine.split(" ");
                String command = parts[0];
                String[] args = new String[parts.length - 1];
                System.arraycopy(parts, 1, args, 0, args.length);

                // Обработка специальных команд
                if ("add".equalsIgnoreCase(command)) {
                    args = readProductData(scanner);
                } else if ("update".equalsIgnoreCase(command)) {
                    if (args.length < 1) {
                        System.out.println("Требуется ID продукта для обновления");
                        continue;
                    }
                    String[] productArgs = readProductData(scanner);
                    // Первый аргумент - ID, остальные - данные продукта
                    args = new String[productArgs.length + 1];
                    args[0] = parts[1]; // ID
                    System.arraycopy(productArgs, 0, args, 1, productArgs.length);
                } else if ("add_if_min".equalsIgnoreCase(command)) {
                    args = readProductData(scanner);
                }

                // Отправляем команду на сервер
                CommandRequest commandRequest = new CommandRequest(command, args, username, passwordHash);
                outputStream.writeObject(commandRequest);
                outputStream.flush();
            }
        } catch (IOException e) {
            System.err.println("Ошибка отправки команды: " + e.getMessage());
        }
    }

    private String[] readProductData(Scanner scanner) {
        String[] args = new String[10];

        // Name
        while (true) {
            System.out.print("Название: ");
            args[0] = scanner.nextLine().trim();
            if (!args[0].isEmpty()) break;
            System.out.println("Ошибка: название не может быть пустым");
        }

        // Coordinates X
        while (true) {
            System.out.print("Координата X (Long > -349): ");
            String input = scanner.nextLine().trim();
            try {
                long x = Long.parseLong(input);
                if (x > -349) {
                    args[1] = input;
                    break;
                } else {
                    System.out.println("Ошибка: X должен быть больше -349");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число");
            }
        }

        // Coordinates Y
        while (true) {
            System.out.print("Координата Y (float): ");
            String input = scanner.nextLine().trim();
            try {
                Float.parseFloat(input);
                args[2] = input;
                break;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число");
            }
        }

        // Price
        while (true) {
            System.out.print("Цена (Long > 0 или пусто): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                args[3] = "";
                break;
            }
            try {
                long price = Long.parseLong(input);
                if (price > 0) {
                    args[3] = input;
                    break;
                } else {
                    System.out.println("Ошибка: цена должна быть больше 0");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число");
            }
        }

        // PartNumber
        while (true) {
            System.out.print("Парт-номер: ");
            args[4] = scanner.nextLine().trim();
            if (!args[4].isEmpty()) break;
            System.out.println("Ошибка: парт-номер не может быть пустым");
        }

        // ManufactureCost
        while (true) {
            System.out.print("Стоимость производства (Float или пусто): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                args[5] = "";
                break;
            }
            try {
                Float.parseFloat(input);
                args[5] = input;
                break;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число");
            }
        }

        // UnitOfMeasure
        while (true) {
            System.out.print("Единица измерения (CENTIMETERS, LITERS, GRAMS): ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("CENTIMETERS") || input.equals("LITERS") || input.equals("GRAMS")) {
                args[6] = input;
                break;
            } else {
                System.out.println("Ошибка: допустимые значения: CENTIMETERS, LITERS, GRAMS");
            }
        }

        // Organization name
        while (true) {
            System.out.print("Название организации: ");
            args[7] = scanner.nextLine().trim();
            if (!args[7].isEmpty()) break;
            System.out.println("Ошибка: название организации не может быть пустым");
        }

        // Organization full name
        while (true) {
            System.out.print("Полное название организации: ");
            args[8] = scanner.nextLine().trim();
            if (!args[8].isEmpty()) break;
            System.out.println("Ошибка: полное название организации не может быть пустым");
        }

        // Employees count
        while (true) {
            System.out.print("Количество сотрудников (Long > 0): ");
            String input = scanner.nextLine().trim();
            try {
                long employees = Long.parseLong(input);
                if (employees > 0) {
                    args[9] = input;
                    break;
                } else {
                    System.out.println("Ошибка: количество сотрудников должно быть больше 0");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число");
            }
        }

        return args;
    }

    private void readResponses() {
        try {
            while (!socket.isClosed()) {
                Object response = inputStream.readObject();
                if (response instanceof Response) {
                    Response resp = (Response) response;
                    if (resp.isSuccess()) {
                        System.out.println("Ответ сервера: " + resp.getMessage());
                    } else {
                        System.err.println("Ошибка сервера: " + resp.getMessage());
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            if (!socket.isClosed()) {
                System.err.println("Ошибка чтения ответа: " + e.getMessage());
            }
        }
    }

    private void close() {
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Ошибка закрытия соединения: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 5432;

        if (args.length > 0) {
            host = args[0];
        }
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Неверный формат порта. Используется порт по умолчанию: " + port);
            }
        }

        Client client = new Client(host, port);
        client.start();
    }
}