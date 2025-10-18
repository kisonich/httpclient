package ru.netology;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class PostRepository {
    private final ConcurrentHashMap<Long, Post> posts = new ConcurrentHashMap<>();
    private final AtomicLong currentId = new AtomicLong(1);

    public List<Post> all() {
        return new ArrayList<>(posts.values());
    }

    public Optional<Post> getById(long id) {
        return Optional.ofNullable(posts.get(id));
    }

    public Post save(Post post) {
        if (post.getId() == 0) {
            // Новый пост
            long id = currentId.getAndIncrement();
            post.setId(id);
            posts.put(id, post);
        } else {
            // Обновление существующего
            posts.put(post.getId(), post);
        }
        return post;
    }

    public void removeById(long id) {
        posts.remove(id);
    }
}

// Класс Post для хранения данных
class Post {
    private long id;
    private String content;

    public Post() {}

    public Post(String content) {
        this.content = content;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}