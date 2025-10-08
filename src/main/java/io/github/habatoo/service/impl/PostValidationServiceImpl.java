package io.github.habatoo.service.impl;

import io.github.habatoo.dto.request.PostRequestValidation;
import io.github.habatoo.repository.PostRepository;
import io.github.habatoo.service.PostValidationService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

/**
 * Сервис валидации для постов
 */
@Service
public class PostValidationServiceImpl implements PostValidationService {

    private final PostRepository postRepository;

    /**
     * Конструктор сервиса валидации.
     *
     * @param postRepository репозиторий постов
     */
    public PostValidationServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * Валидирует параметры пагинации.
     *
     * @param search     строка поиска
     * @param pageNumber номер страницы
     * @param pageSize   размер страницы
     * @throws IllegalArgumentException если параметры невалидны
     */
    public void validatePaginationParams(String search, int pageNumber, int pageSize) {
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
     * @param postRequest запрос на создание/изменение поста
     * @throws IllegalArgumentException если данные невалидны
     */
    public void validatePostRequest(PostRequestValidation postRequest) {
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
     *
     * @param postId идентификатор поста
     * @throws IllegalArgumentException если идентификатор невалиден
     */
    public void validatePostId(Long postId) {
        if (postId == null) {
            throw new IllegalArgumentException("Post ID cannot be null");
        }
    }

    /**
     * Проверяет существование поста.
     *
     * @param postId идентификатор поста
     * @throws EmptyResultDataAccessException если пост не найден
     */
    public void validatePostExists(Long postId) {
        if (!postRepository.postExists(postId)) {
            throw new EmptyResultDataAccessException("Post with id " + postId + " not found", 1);
        }
    }
}
