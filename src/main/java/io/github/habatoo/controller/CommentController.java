package io.github.habatoo.controller;

import io.github.habatoo.model.Comment;
import io.github.habatoo.service.CommentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Контроллер по работе с комментариями к постам.
 *
 * <p>Предоставляет REST API endpoints для операций с комментариями блога.
 * Обрабатывает HTTP запросы, связанные с комментариями конкретных постов,
 * и возвращает данные в формате JSON.</p>
 */
@RestController
@RequestMapping("/api/posts")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Получение всех комментариев для указанного поста.
     *
     * <p>Обрабатывает GET запросы по пути {@code /api/posts/{postId}/comments}
     * и возвращает список всех комментариев, связанных с указанным постом,
     * в формате JSON.</p>
     *
     * @param postId идентификатор поста
     * @return JSON список комментариев к посту
     */
    @GetMapping("/{postId}/comments")
    public List<Comment> getCommentsByPostId(@PathVariable("postId") Long postId) {
        return commentService.getCommentsByPostId(postId);
    }
}
