package io.github.habatoo.service;

import io.github.habatoo.dto.request.PostRequestValidation;
import org.springframework.dao.EmptyResultDataAccessException;

/**
 * Контракт валидации для постов
 */
public interface PostValidationService {

    /**
     * Валидирует параметры пагинации.
     *
     * @param search     строка поиска
     * @param pageNumber номер страницы
     * @param pageSize   размер страницы
     * @throws IllegalArgumentException если параметры невалидны
     */
     void validatePaginationParams(String search, int pageNumber, int pageSize);

    /**
     * Валидирует запрос на создание/изменение поста.
     *
     * @param postRequest запрос на создание/изменение поста
     * @throws IllegalArgumentException если данные невалидны
     */
     void validatePostRequest(PostRequestValidation postRequest);

    /**
     * Валидирует идентификатор поста.
     *
     * @param postId идентификатор поста
     * @throws IllegalArgumentException если идентификатор невалиден
     */
     void validatePostId(Long postId);

    /**
     * Проверяет существование поста.
     *
     * @param postId идентификатор поста
     * @throws EmptyResultDataAccessException если пост не найден
     */
    void validatePostExists(Long postId);
}
