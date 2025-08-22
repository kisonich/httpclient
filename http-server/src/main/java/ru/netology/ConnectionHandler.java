package ru.netology;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ConnectionHandler implements Runnable {
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final List<String> ALLOWED_METHODS = List.of(GET, POST);

    private final Socket socket;
    private final HandlerManager handlerManager;

    public ConnectionHandler(Socket socket, HandlerManager handlerManager) {
        this.socket = socket;
        this.handlerManager = handlerManager;
    }

    @Override
    public void run() {
        try (var in = new BufferedInputStream(socket.getInputStream());
             var out = new BufferedOutputStream(socket.getOutputStream())) {

            Request request = parseRequest(in);
            if (request != null) {
                processRequest(request, out);
            } else {
                sendBadRequest(out);
            }

        } catch (IOException e) {
            System.err.println("Error handling connection: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    private Request parseRequest(BufferedInputStream in) throws IOException {
        final var limit = 4096;
        in.mark(limit);
        final var buffer = new byte[limit];
        final var read = in.read(buffer);

        if (read == -1) {
            return null;
        }

        // Parse request line
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            return null;
        }

        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            return null;
        }

        final var method = requestLine[0];
        if (!ALLOWED_METHODS.contains(method)) {
            return null;
        }

        final var path = requestLine[1];
        if (!path.startsWith("/")) {
            return null;
        }

        // Parse headers
        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            return null;
        }

        in.reset();
        in.skip(headersStart);

        final var headersBytes = in.readNBytes(headersEnd - headersStart);
        final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));

        // Parse body
        String body = "";
        if (method.equals(POST)) {
            in.skip(headersDelimiter.length);
            final var contentLength = extractHeader(headers, "Content-Length");
            if (contentLength.isPresent()) {
                try {
                    final var length = Integer.parseInt(contentLength.get());
                    if (length > 0 && length < 10000) { // Защита от больших тел
                        final var bodyBytes = in.readNBytes(length);
                        body = new String(bodyBytes);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Invalid Content-Length: " + contentLength.get());
                }
            }
        }

        return new Request(method, path, headers, body);
    }

    private void processRequest(Request request, BufferedOutputStream out) throws IOException {
        if (!handlerManager.handleRequest(request, out)) {
            sendNotFound(out, request.getPath());
        }
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

    private void sendBadRequest(BufferedOutputStream out) throws IOException {
        sendResponse(out, "400 Bad Request", "text/plain", "Bad Request");
    }

    private void sendNotFound(BufferedOutputStream out, String path) throws IOException {
        sendResponse(out, "404 Not Found", "text/plain", "Not found: " + path);
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(h -> h.toLowerCase().startsWith(header.toLowerCase() + ":"))
                .map(h -> h.substring(header.length() + 1).trim())
                .findFirst();
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}