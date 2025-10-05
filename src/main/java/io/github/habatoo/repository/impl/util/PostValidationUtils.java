package io.github.habatoo.repository.impl.util;

import io.github.habatoo.dto.request.PostRequestValidation;
import lombok.experimental.UtilityClass;

/**
 * Утилитный класс для валидации данных.
 */
@UtilityClass
public final class PostValidationUtils {

    /**
     * Валидирует данные запроса
     *
     * @param search     строка из поля поиска
     * @param pageNumber номер текущей страницы
     * @param pageSize   число постов на странице
     * @throws IllegalStateException входные данные содержат/равны null
     */
    public static void validatePaginationParams(String search, int pageNumber, int pageSize) {
        if (search == null) {
            throw new IllegalArgumentException("Search parameter cannot be null");
        }
        if (pageNumber < 1) {
            throw new IllegalArgumentException("Page number must be greater than 0");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }
    }

    /**
     * Валидирует запрос на создание/изменение поста.
     *
     * @param postRequest запрос на создание/изменение поста.
     * @throws IllegalStateException входные данные содержат/равны null
     */
    public static void validatePostRequest(PostRequestValidation postRequest) {
        if (postRequest == null) {
            throw new IllegalArgumentException("PostRequest cannot be null");
        }
        if (postRequest.title() == null || postRequest.title().trim().isEmpty()) {
            throw new IllegalArgumentException("Post title cannot be null or empty");
        }
        if (postRequest.text() == null || postRequest.text().trim().isEmpty()) {
            throw new IllegalArgumentException("Post text cannot be null or empty");
        }
        if (postRequest.tags() == null) {
            throw new IllegalArgumentException("Post tags cannot be null");
        }
    }

    /**
     * Валидирует идентификатор поста.
     */
    public static void validatePostId(Long postId) {
        if (postId == null) {
            throw new IllegalArgumentException("Post ID cannot be null");
        }
    }
}