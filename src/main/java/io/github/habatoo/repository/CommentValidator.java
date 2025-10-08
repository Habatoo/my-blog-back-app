package io.github.habatoo.repository;

import io.github.habatoo.dto.request.CommentCreateRequest;

/**
 * Интерфейс валидатора для комментариев.
 *
 * <p>Предоставляет методы для проверки корректности данных, связанных с комментариями,
 * включая идентификаторы постов и комментариев, текст комментариев и запросы на создание.</p>
 */
public interface CommentValidator {

    /**
     * Валидирует идентификатор поста.
     *
     * <p>Проверяет, что идентификатор поста не является null и имеет положительное значение.
     * Используется для проверки существования поста перед операциями с комментариями.</p>
     *
     * @param postId идентификатор поста для проверки
     * @throws IllegalArgumentException если postId является null или не положителен
     */
    void validatePostId(Long postId);

    /**
     * Валидирует пару идентификаторов поста и комментария.
     *
     * <p>Выполняет проверку обоих идентификаторов на соответствие требованиям:
     * не null и положительные значения. Используется в операциях, требующих
     * указания как поста, так и комментария.</p>
     *
     * @param postId    идентификатор поста для проверки
     * @param commentId идентификатор комментария для проверки
     * @throws IllegalArgumentException если любой из идентификаторов невалиден
     */
    void validateIds(Long postId, Long commentId);

    /**
     * Валидирует текст комментария.
     *
     * @param text текст комментария для проверки
     * @throws IllegalArgumentException если текст пуст, null или превышает максимальную длину
     */
    void validateCommentText(String text);

    /**
     * Валидирует запрос на создание комментария.
     *
     * @param request запрос на создание комментария
     * @throws IllegalArgumentException если любая часть запроса невалидна
     */
    void validateCommentRequest(CommentCreateRequest request);

}
