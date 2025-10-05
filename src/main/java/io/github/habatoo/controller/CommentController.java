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
import java.util.function.Supplier;

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

        return handleCommentOperation(
                () -> commentService.createComment(postId, commentRequest.text()),
                HttpStatus.CREATED,
                null
        );
    }

    /**
     * Обработчик HTTP PUT запроса для обновления комментария к посту.
     * Обновляет текст комментария и возвращает обновленный комментарий.
     *
     * @param postId         идентификатор поста из пути запроса
     * @param commentId      идентификатор комментария из пути запроса
     * @param commentRequest объект запроса с обновленными данными комментария
     * @return ResponseEntity с обновленным комментарием и статусом 200 если успешно,
     * или статусом 400/404 если комментарий не найден или не принадлежит посту
     */
    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            @RequestBody CommentRequest commentRequest) {

        return handleCommentOperation(
                () -> commentService.updateComment(postId, commentId, commentRequest.text())
                        .orElseThrow(() -> new EmptyResultDataAccessException("Comment not found", 1)),
                HttpStatus.OK,
                null
        );
    }

    /**
     * Обработчик HTTP DELETE запроса для удаления комментария.
     * Удаляет комментарий и возвращает статус 200 при успехе.
     *
     * @param postId    идентификатор поста из пути запроса
     * @param commentId идентификатор комментария из пути запроса
     * @return ResponseEntity со статусом 200 если удален, 400/404 если не найден
     */
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId) {

        return handleCommentOperation(
                () -> {
                    boolean deleted = commentService.deleteComment(postId, commentId);
                    if (!deleted) {
                        throw new EmptyResultDataAccessException("Comment not found", 1);
                    }
                    return null;
                },
                HttpStatus.OK,
                null
        );
    }

    /**
     * Обрабатывает операции с комментариями и возвращает соответствующий ResponseEntity
     *
     * @param operation     лямбда-выражение с операцией над комментарием
     * @param successStatus HTTP статус для успешного выполнения
     * @param successBody   тело ответа для успешного выполнения
     * @return ResponseEntity с результатом операции
     */
    private ResponseEntity<?> handleCommentOperation(
            Supplier<Object> operation,
            HttpStatus successStatus,
            Object successBody
    ) {
        try {
            Object result = operation.get();
            return ResponseEntity.status(successStatus).body(successBody != null ? successBody : result);

        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Comment or post not found"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));

        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Database error"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

}
