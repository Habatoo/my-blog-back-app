package io.github.habatoo.controller;

import io.github.habatoo.model.Post;
import io.github.habatoo.service.PostService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Контроллер по работе с постами.
 *
 * <p>Предоставляет REST API endpoints для операций с постами блога.
 * Обрабатывает HTTP запросы и возвращает данные в формате JSON.</p>
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * Получение всех постов.
     *
     * <p>Обрабатывает GET запросы по пути {@code /api/posts} и возвращает
     * полный список всех постов в системе в формате JSON.</p>
     *
     * @return JSON список всех постов
     */
    @GetMapping
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }
}
