package io.github.habatoo.repository.impl.util;

import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;

/**
 * Утилитный класс для валидации изображений.
 */
@UtilityClass
public final class ImageValidationUtils {

    /**
     * Валидирует параметры для обновления изображения поста.
     */
    public static void validateImageUpdateParams(Long postId, MultipartFile image) {
        if (postId == null) {
            throw new IllegalArgumentException("Post ID cannot be null");
        }
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image file cannot be null or empty");
        }
    }

    /**
     * Валидирует параметры для получения изображения поста.
     */
    public static void validateImageRetrievalParams(Long postId) {
        if (postId == null) {
            throw new IllegalArgumentException("Post ID cannot be null");
        }
    }
}
