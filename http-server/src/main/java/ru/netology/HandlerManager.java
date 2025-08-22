package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HandlerManager {
    private final Map<String, RequestHandler> handlers = new ConcurrentHashMap<>();

    public void registerHandler(String path, RequestHandler handler) {
        if (path != null && handler != null) {
            handlers.put(path, handler);
        }
    }

    public boolean handleRequest(Request request, BufferedOutputStream out) throws IOException {
        if (request == null || out == null) {
            return false;
        }

        String path = request.getPath();
        RequestHandler handler = handlers.get(path);

        if (handler != null) {
            handler.handle(request, out);
            return true;
        }

        // Попробуем найти обработчик для корневого пути, если конкретный не найден
        if (!path.equals("/")) {
            handler = handlers.get("/");
            if (handler != null) {
                handler.handle(request, out);
                return true;
            }
        }

        return false;
    }

    public boolean hasHandler(String path) {
        return handlers.containsKey(path);
    }
}