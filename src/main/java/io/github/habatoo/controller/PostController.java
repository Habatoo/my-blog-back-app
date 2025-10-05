package io.github.habatoo.controller;

import io.github.habatoo.controller.dto.PostRequest;
import io.github.habatoo.model.Post;
import io.github.habatoo.service.PostService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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
    @GetMapping("/all")
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    /**
     * Получение списка постов с пагинацией и поиском
     *
     * <p>Обрабатывает GET запросы по пути {@code /api/posts} с параметрами:
     * search, pageNumber, pageSize для пагинации и поиска постов.</p>
     *
     * @param search     строка поиска (обязательный)
     * @param pageNumber номер страницы (обязательный)
     * @param pageSize   размер страницы (обязательный)
     * @return ответ с пагинированным списком постов
     */
    @GetMapping
    public ResponseEntity<?> getPosts(
            @RequestParam("search") String search,
            @RequestParam("pageNumber") int pageNumber,
            @RequestParam("pageSize") int pageSize) {
        return handlePostOperation(
                () -> postService.getPosts(search, pageNumber, pageSize),
                HttpStatus.OK,
                null
        );
    }

    /**
     * Обработчик HTTP GET запроса для получения конкретного поста.
     * Возвращает полную информацию о посте включая теги и количество комментариев.
     *
     * @param id идентификатор поста из пути запроса
     * @return ResponseEntity с постом или 404 если не найден
     */
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(
            @PathVariable("id") Long id) {
        return postService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Создание нового поста
     *
     * @param postRequest DTO с данными для создания поста TODO
     * @return ResponseEntity с созданным постом в формате JSON
     */
    @PostMapping
    public ResponseEntity<?> createPost(
            @RequestBody PostRequest postRequest) {
        return handlePostOperation(
                () -> postService.createPost(postRequest),
                HttpStatus.CREATED,
                null
        );
    }

    /**
     * Исправление существующего поста
     *
     * @param id идентификатор поста для исправления из пути запроса
     * @return ResponseEntity с исправленным постом в формате JSON
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(
            @PathVariable("id") Long id,
            @RequestBody PostRequest postRequest
    ) {
        postRequest.setId(id);
        return handlePostOperation(
                () -> postService.updatePost(postRequest),
                HttpStatus.OK,
                null
        );
    }

    /**
     * Удаляет пост по идентификатору вместе со всеми комментариями
     *
     * @param id идентификатор поста для удаления
     * @return ResponseEntity со статусом 200 OK при успешном удалении
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable("id") Long id) {
        return handlePostOperation(
                () -> {
                    postService.deletePost(id);
                    return null;
                },
                HttpStatus.OK,
                null
        );
    }

    /**
     * Увеличивает счетчик лайков поста на 1
     *
     * @param id идентификатор поста для увеличения лайков
     * @return обновленное количество лайков поста в теле ответа при успешном увеличении
     */
    @PostMapping("/{id}/likes")
    public ResponseEntity<?> incrementLikes(
            @PathVariable("id") Long id) {
        return handlePostOperation(
                () -> postService.incrementLikes(id),
                HttpStatus.OK,
                null
        );
    }

    /**
     * Обрабатывает операции с постами и возвращает соответствующий ResponseEntity
     *
     * @param operation     лямбда-выражение с операцией над постом
     * @param successStatus HTTP статус для успешного выполнения
     * @param successBody   тело ответа для успешного выполнения
     * @return ResponseEntity с результатом операции
     */
    private ResponseEntity<?> handlePostOperation(
            Supplier<Object> operation,
            HttpStatus successStatus,
            Object successBody
    ) {
        try {
            Object result = operation.get();
            return ResponseEntity.status(successStatus).body(successBody != null ? successBody : result);

        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Post not found"));

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
