package io.github.habatoo.controller;

import io.github.habatoo.dto.request.CommentCreateRequest;
import io.github.habatoo.dto.request.CommentRequest;
import io.github.habatoo.dto.response.CommentResponse;
import io.github.habatoo.handler.GlobalExceptionHandler;
import io.github.habatoo.service.CommentService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для управления комментариями к постам блога.
 *
 * <p>Предоставляет REST API endpoints для выполнения CRUD операций с комментариями.
 * Все операции привязаны к конкретным постам через идентификатор поста в пути запроса.
 * Обрабатывает HTTP запросы и возвращает данные в формате JSON.</p>
 *
 * @see CommentService
 * @see CommentResponse
 * @see CommentCreateRequest
 * @see GlobalExceptionHandler
 */
@RestController
@RequestMapping("/api/posts")
public class CommentController {

    private final CommentService commentService;

    /**
     * Конструктор контроллера комментариев.
     *
     * @param commentService сервис для бизнес-логики работы с комментариями
     */
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Получает все комментарии для указанного поста.
     *
     * <p>Обрабатывает GET запросы по пути {@code /api/posts/{postId}/comments}
     * и возвращает список всех комментариев, связанных с указанным постом,
     * в формате JSON. Комментарии возвращаются в порядке их создания.</p>
     *
     * @param postId идентификатор поста, для которого запрашиваются комментарии. Должен быть положительным числом
     * @return список комментариев к посту в формате JSON. Пустой список если комментарии отсутствуют
     * @throws IllegalArgumentException если идентификатор поста невалиден
     * @throws DataAccessException      при ошибках доступа к данным
     */
    @GetMapping("/{postId}/comments")
    public List<CommentResponse> getCommentsByPostId(@PathVariable("postId") Long postId) {
        return commentService.getCommentsByPostId(postId);
    }

    /**
     * Получает комментарий по идентификаторам поста и комментария.
     *
     * <p>Обрабатывает GET запросы по пути {@code /api/posts/{postId}/comments/{commentId}}
     * для получения конкретного комментария. Проверяет принадлежность комментария указанному посту.</p>
     *
     * @param postId    идентификатор поста, к которому принадлежит комментарий. Должен быть положительным числом
     * @param commentId идентификатор запрашиваемого комментария. Должен быть положительным числом
     * @return ResponseEntity с комментарием и статусом 200 OK если найден,
     * или статусом 404 Not Found если комментарий не существует или не принадлежит посту
     * @throws IllegalArgumentException если идентификаторы невалидны
     * @throws DataAccessException      при ошибках доступа к данным
     */
    @GetMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> getCommentByPostIdAndId(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId) {

        return commentService.getCommentByPostIdAndId(postId, commentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Создает новый комментарий для указанного поста.
     *
     * <p>Обрабатывает POST запросы по пути {@code /api/posts/{postId}/comments}
     * для создания нового комментария. Валидирует входные данные и создает комментарий
     * с присвоенным идентификатором.</p>
     *
     * @param postId               идентификатор поста, к которому добавляется комментарий. Должен быть положительным числом
     * @param commentCreateRequest DTO с данными для создания комментария. Текст комментария обязателен
     * @return ResponseEntity с созданным комментарием и статусом 201 Created
     * @throws IllegalArgumentException       если идентификатор поста невалиден или данные запроса некорректны
     * @throws EmptyResultDataAccessException если пост с указанным ID не найден
     * @throws DataAccessException            при ошибках сохранения данных
     */
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable("postId") Long postId,
            @RequestBody CommentCreateRequest commentCreateRequest) {

        CommentResponse result = commentService.createComment(commentCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Обновляет существующий комментарий к посту.
     *
     * <p>Обрабатывает PUT запросы по пути {@code /api/posts/{postId}/comments/{commentId}}
     * для обновления текста комментария. Проверяет существование комментария и его принадлежность посту.</p>
     *
     * @param postId         идентификатор поста, к которому принадлежит комментарий. Должен быть положительным числом
     * @param commentId      идентификатор обновляемого комментария. Должен быть положительным числом
     * @param commentRequest DTO с обновленными данными комментария. Текст комментария обязателен
     * @return ResponseEntity с обновленным комментарием и статусом 200 OK
     * @throws IllegalArgumentException       если идентификаторы невалидны или данные запроса некорректны
     * @throws EmptyResultDataAccessException если комментарий не найден или не принадлежит посту
     * @throws DataAccessException            при ошибках обновления данных
     */
    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            @RequestBody CommentRequest commentRequest) {

        CommentResponse result = commentService.updateComment(postId, commentId, commentRequest.text())
                .orElseThrow(() -> new EmptyResultDataAccessException("Comment not found", 1));
        return ResponseEntity.ok(result);
    }

    /**
     * Удаляет комментарий у указанного поста.
     *
     * <p>Обрабатывает DELETE запросы по пути {@code /api/posts/{postId}/comments/{commentId}}
     * для удаления комментария. Проверяет существование комментария и его принадлежность посту
     * перед выполнением удаления.</p>
     *
     * @param postId    идентификатор поста, к которому принадлежит комментарий. Должен быть положительным числом
     * @param commentId идентификатор удаляемого комментария. Должен быть положительным числом
     * @return ResponseEntity со статусом 200 OK при успешном удалении
     * @throws IllegalArgumentException       если идентификаторы невалидны
     * @throws EmptyResultDataAccessException если комментарий не найден или не принадлежит посту
     * @throws DataAccessException            при ошибках удаления данных
     */
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId) {

        boolean deleted = commentService.deleteComment(postId, commentId);
        if (!deleted) {
            throw new EmptyResultDataAccessException("Comment not found", 1);
        }
        return ResponseEntity.ok().build();
    }
}
