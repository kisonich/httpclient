package ru.netology.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.netology.PostRepository;
import ru.netology.PostService;
import ru.netology.PostController;

@Configuration
@ComponentScan("ru.netology")
public class AppConfig {

    @Bean
    public PostRepository postRepository() {
        return new PostRepository();
    }

    @Bean
    public PostService postService(PostRepository repository) {
        return new PostService(repository);
    }

    @Bean
    public PostController postController(PostService service) {
        return new PostController(service);
    }
}