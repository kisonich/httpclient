package ru.netology;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.netology.config.AppConfig;

public class PostServlet {
    private PostController controller;

    public void init() {
        var context = new AnnotationConfigApplicationContext(AppConfig.class);
        controller = context.getBean(PostController.class);
    }

    public PostController getController() {
        return controller;
    }
}