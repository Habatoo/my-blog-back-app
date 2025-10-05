package io.github.habatoo.controller;

import io.github.habatoo.service.ImageService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/posts")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * Обновление картинки поста
     *
     * <p>Обрабатывает PUT запросы по пути {@code /api/posts/{postId}/image}
     * для обновления изображения поста. Фронтенд отправляет файл в формате multipart/form-data.</p>
     *
     * @param postId идентификатор поста
     * @param image  файл изображения, загружаемый фронтендом
     * @return ResponseEntity со статусом 200 OK при успешном обновлении
     */
    @PutMapping("/{postId}/image")
    public ResponseEntity<?> updatePostImage(
            @PathVariable("postId") Long postId,
            @RequestParam("image") MultipartFile image) {

        return handleImageOperation(
                () -> {
                    imageService.updatePostImage(postId, image);
                    return null;
                },
                HttpStatus.OK,
                null
        );
    }

    /**
     * Получение картинки поста
     *
     * <p>Обрабатывает GET запросы по пути {@code /api/posts/{postId}/image}
     * для получения изображения поста. Возвращает массив байт картинки.</p>
     *
     * @param postId идентификатор поста
     * @return ResponseEntity с массивом байт изображения
     */
    @GetMapping("/{postId}/image")
    public ResponseEntity<?> getPostImage(
            @PathVariable("postId") Long postId) {

        return handleImageOperation(
                () -> imageService.getPostImage(postId),
                HttpStatus.OK,
                null
        );
    }

    /**
     * Обрабатывает операции с изображениями и возвращает соответствующий ResponseEntity
     *
     * @param operation     лямбда-выражение с операцией над изображением
     * @param successStatus HTTP статус для успешного выполнения
     * @param successBody   тело ответа для успешного выполнения
     * @return ResponseEntity с результатом операции
     */
    private ResponseEntity<?> handleImageOperation(
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
