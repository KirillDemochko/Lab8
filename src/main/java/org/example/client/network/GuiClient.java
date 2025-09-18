package org.example.client.network;

import org.example.network.AuthRequest;
import org.example.network.CommandRequest;
import org.example.network.Response;
import org.example.data.Product;
import org.example.data.User;
import org.example.util.HashUtil;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class GuiClient {
    private static GuiClient instance;
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private ExecutorService executorService;
    private String host;
    private int port;
    private boolean connected;

    private GuiClient() {
        executorService = Executors.newCachedThreadPool();
        connected = false;
    }

    public static GuiClient getInstance() {
        if (instance == null) {
            instance = new GuiClient();
        }
        return instance;
    }

    public boolean connect(String host, int port) {
        this.host = host;
        this.port = port;

        try {
            socket = new Socket(host, port);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            connected = true;
            // startResponseListener();

            return true;
        } catch (IOException e) {
            // обработка ошибки
            connected = false;
            return false;
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (socket != null) socket.close();
            if (executorService != null) executorService.shutdown();
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void sendAuthRequest(String username, String password, boolean isRegistration,
                                Consumer<Response> callback) {
        executorService.execute(() -> {
            try {
                AuthRequest request = new AuthRequest(username, password);
                request.setIsRegistration(isRegistration);

                // Синхронизация для предотвращения конфликтов
                synchronized (outputStream) {
                    outputStream.writeObject(request);
                    outputStream.flush();

                    // Чтение ответа сразу после отправки
                    Response response = (Response) inputStream.readObject();
                    SwingUtilities.invokeLater(() -> callback.accept(response));
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    callback.accept(new Response(false, "Authentication error: " + e.getMessage()));
                });
            }
        });
    }

    public void sendCommandRequest(String command, String[] args, User user,
                                   Consumer<Response> callback) {
        executorService.execute(() -> {
            try {
                CommandRequest request = new CommandRequest(
                        command, args, user.getUsername(), user.getPasswordHash());

                // Синхронизация для предотвращения конфликтов
                synchronized (outputStream) {
                    outputStream.writeObject(request);
                    outputStream.flush();

                    // Чтение ответа сразу после отправки
                    Response response = (Response) inputStream.readObject();
                    SwingUtilities.invokeLater(() -> callback.accept(response));
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    callback.accept(new Response(false, "Command error: " + e.getMessage()));
                });
            }
        });
    }

    public void requestProducts(Consumer<List<Product>> callback, Consumer<String> errorCallback) {
        executorService.execute(() -> {
            try {
                outputStream.writeObject("GET_PRODUCTS");
                outputStream.flush();

                @SuppressWarnings("unchecked")
                List<Product> products = (List<Product>) inputStream.readObject();
                SwingUtilities.invokeLater(() -> callback.accept(products));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    errorCallback.accept("Failed to get products: " + e.getMessage());
                });
            }
        });
    }

    private void startResponseListener() {
        executorService.execute(() -> {
            while (connected) {
                try {
                    Object response = inputStream.readObject();
                    if (response instanceof Response) {
                        // Обрабатываем ответы от сервера
                        Response resp = (Response) response;
                        if (!resp.isSuccess()) {
                            System.err.println("Server error: " + resp.getMessage());
                        }
                    }
                } catch (Exception e) {
                    if (connected) {
                        System.err.println("Error in response listener: " + e.getMessage());
                        connected = false;
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(null,
                                    "Connection lost: " + e.getMessage(),
                                    "Connection Error",
                                    JOptionPane.ERROR_MESSAGE);
                        });
                    }
                    break;
                }
            }
        });
    }

    public void startPeriodicUpdates(int intervalSeconds, Runnable updateTask) {
        executorService.execute(() -> {
            while (connected) {
                try {
                    Thread.sleep(intervalSeconds * 1000);
                    if (connected) {
                        SwingUtilities.invokeLater(updateTask);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
}