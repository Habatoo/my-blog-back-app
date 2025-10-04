package io.github.habatoo.controller;

import io.github.habatoo.controller.dto.CommentRequest;
import io.github.habatoo.model.Comment;
import io.github.habatoo.service.CommentService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    /**
     * Обработчик HTTP GET запроса для получения комментария поста.
     * Возвращает комментарий в формате JSON если найден, иначе HTTP 404.
     *
     * @param postId    идентификатор поста из пути запроса
     * @param commentId идентификатор комментария из пути запроса
     * @return {@code ResponseEntity} с комментарием и статусом 200 если найден,
     * или статусом 404 если комментарий не существует или не принадлежит посту
     */
    @GetMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Comment> getCommentByPostIdAndId(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId) {

        return commentService.getCommentByPostIdAndId(postId, commentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Обработчик HTTP POST запроса для создания нового комментария к посту.
     * Создает комментарий и возвращает его в формате JSON.
     *
     * @param postId         идентификатор поста из пути запроса
     * @param commentRequest объект запроса с текстом комментария
     * @return созданный комментарий с присвоенным идентификатором или ошибку 400/404
     */
    @PostMapping("/{postId}/comments")
    public ResponseEntity<?> createComment(
            @PathVariable("postId") Long postId,
            @RequestBody CommentRequest commentRequest) {

        try {
            // Проверка соответствия postId в пути и в теле запроса
            if (!postId.equals(commentRequest.postId())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Post ID in path and body must match"));
            }

            Comment createdComment = commentService.createComment(postId, commentRequest.text());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);

        } catch (EmptyResultDataAccessException e) {
            // Ошибка целостности данных (пост не найден)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Post not found with id: " + postId));

        } catch (DataAccessException e) {
            // Ошибка базы данных
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Database error while creating comment"));

        } catch (Exception e) {
            // Любая другая ошибка
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid request data"));
        }
    }

    /**
     * Обработчик HTTP PUT запроса для обновления комментария к посту.
     * Обновляет текст комментария и возвращает обновленный комментарий.
     *
     * @param postId         идентификатор поста из пути запроса
     * @param commentId      идентификатор комментария из пути запроса
     * @param commentRequest объект запроса с обновленными данными комментария
     * @return ResponseEntity с обновленным комментарием и статусом 200 если успешно,
     * или статусом 404 если комментарий не найден или не принадлежит посту
     */
    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            @RequestBody CommentRequest commentRequest) {

        return commentService.updateComment(postId, commentId, commentRequest.text())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Обработчик HTTP DELETE запроса для удаления комментария.
     * Удаляет комментарий и возвращает статус 200 при успехе.
     *
     * @param postId    идентификатор поста из пути запроса
     * @param commentId идентификатор комментария из пути запроса
     * @return ResponseEntity со статусом 200 если удален, 404 если не найден
     */
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId) {

        boolean deleted = commentService.deleteComment(postId, commentId);
        return deleted ?
                ResponseEntity.ok().build() :
                ResponseEntity.notFound().build();
    }

}
