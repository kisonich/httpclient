package ru.netology;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
public class PostController {
    private final PostService service;

    @Autowired
    public PostController(PostService service) {
        this.service = service;
    }

    public void handleGetPosts(Request request, BufferedOutputStream out) throws IOException {
        List<Post> posts = service.all();
        String response = "All posts: " + posts.size() + "\n";
        for (Post post : posts) {
            response += "Post " + post.getId() + ": " + post.getContent() + "\n";
        }
        sendResponse(out, "200 OK", "text/plain", response);
    }

    public void handleGetPost(Request request, BufferedOutputStream out) throws IOException {
        String path = request.getPath();
        String[] parts = path.split("/");
        if (parts.length >= 3) {
            try {
                long id = Long.parseLong(parts[2]);
                Optional<Post> post = service.getById(id);
                if (post.isPresent()) {
                    sendResponse(out, "200 OK", "text/plain",
                            "Post " + id + ": " + post.get().getContent());
                } else {
                    sendResponse(out, "404 Not Found", "text/plain", "Post not found");
                }
            } catch (NumberFormatException e) {
                sendResponse(out, "400 Bad Request", "text/plain", "Invalid post ID");
            }
        }
    }

    public void handleCreatePost(Request request, BufferedOutputStream out) throws IOException {
        if ("POST".equals(request.getMethod())) {
            String content = request.getBody();
            if (content != null && !content.trim().isEmpty()) {
                Post post = new Post(content.trim());
                service.save(post);
                sendResponse(out, "201 Created", "text/plain",
                        "Post created with ID: " + post.getId());
            } else {
                sendResponse(out, "400 Bad Request", "text/plain", "Content is required");
            }
        }
    }

    private void sendResponse(BufferedOutputStream out, String status,
                              String contentType, String message) throws IOException {
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
}