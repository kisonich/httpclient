package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int THREAD_POOL_SIZE = 64;
    private static final int PORT = 9999;

    private final ExecutorService threadPool;
    private final HandlerManager handlerManager;
    private ServerSocket serverSocket;
    private volatile boolean isRunning;

    public Server() {
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.handlerManager = new HandlerManager();
        registerHandlers();
    }

    private void registerHandlers() {
        // Создаем сервлет и инициализируем его
        PostServlet postServlet = new PostServlet();
        postServlet.init();
        PostController postController = postServlet.getController();

        // Обработчик для корневого пути
        handlerManager.registerHandler("/", (request, out) -> {
            String response = "HTTP Server with Posts CRUD\n\n" +
                    "Available endpoints:\n" +
                    "GET /posts - get all posts\n" +
                    "GET /posts/{id} - get post by ID\n" +
                    "POST /posts - create new post (send content in body)\n" +
                    "Example: curl -X POST -d 'Hello World' http://localhost:9999/posts";
            sendResponse(out, "200 OK", "text/plain", response);
        });

        // Обработчики для постов - ДОБАВЛЯЕМ ЭТИ СТРОКИ
        handlerManager.registerHandler("/posts", (request, out) -> {
            if ("GET".equals(request.getMethod())) {
                postController.handleGetPosts(request, out);
            } else if ("POST".equals(request.getMethod())) {
                postController.handleCreatePost(request, out);
            } else {
                sendResponse(out, "405 Method Not Allowed", "text/plain", "Method not allowed");
            }
        });

        // Обработчик для получения конкретного поста
        handlerManager.registerHandler("/posts/", (request, out) -> {
            if ("GET".equals(request.getMethod())) {
                postController.handleGetPost(request, out);
            } else {
                sendResponse(out, "405 Method Not Allowed", "text/plain", "Method not allowed");
            }
        });

        // Существующие обработчики /messages и /test (оставляем как есть)
        handlerManager.registerHandler("/messages", (request, out) -> {
            StringBuilder response = new StringBuilder();
            response.append("Messages Handler\n\n");
            response.append("Path: ").append(request.getPath()).append("\n");

            request.getQueryParam("last").ifPresent(last -> {
                response.append("Last parameter: ").append(last).append("\n");
            });

            request.getQueryParam("filter").ifPresent(filter -> {
                response.append("Filter parameter: ").append(filter).append("\n");
            });

            response.append("All query parameters: ").append(request.getQueryParams()).append("\n");

            sendResponse(out, "200 OK", "text/plain", response.toString());
        });

        handlerManager.registerHandler("/test", (request, out) -> {
            String response = "Test Handler\n\n" +
                    "Query params: " + request.getQueryParams() + "\n" +
                    "Single param 'name': " + request.getQueryParam("name").orElse("not provided");
            sendResponse(out, "200 OK", "text/plain", response);
        });
    }

    public void start() {
        isRunning = true;
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);
            System.out.println("Available handlers: /, /messages, /test");

            while (isRunning) {
                try {
                    var socket = serverSocket.accept();
                    threadPool.submit(new ConnectionHandler(socket, handlerManager));
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("Error accepting connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        } finally {
            stop();
        }
    }

    public void stop() {
        isRunning = false;
        if (threadPool != null) {
            threadPool.shutdown();
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }
        System.out.println("Server stopped");
    }
















    private void sendResponse(BufferedOutputStream out, String status, String contentType, String message) throws IOException {
        String response = String.format(
                "HTTP/1.1 %s\r\n" +
                        "Content-Type: %s\r\n" +
                        "Content-Length: %d\r\n" +
                        "Connection: close\r\n" +
                        "\r\n" +
                        "%s",
                status, contentType, message.getBytes().length, message
        );
        out.write(response.getBytes());
        out.flush();
    }

    public static void main(String[] args) {
        Server server = new Server();

        // Добавляем shutdown hook для graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down server...");
            server.stop();
        }));

        server.start();
    }


}