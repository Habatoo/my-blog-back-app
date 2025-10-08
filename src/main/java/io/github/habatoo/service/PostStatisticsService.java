package io.github.habatoo.service;

import org.springframework.transaction.annotation.Transactional;

/**
 * Контракт для работы со статистикой постов
 */
public interface PostStatisticsService {

    /**
     * Увеличивает счетчик лайков поста.
     *
     * @param postId идентификатор поста
     * @return новое количество лайков
     */
    @Transactional
    int incrementLikes(Long postId);

    /**
     * Увеличивает счетчик комментариев поста.
     *
     * @param postId идентификатор поста
     */
    @Transactional
    void incrementCommentsCount(Long postId);

    /**
     * Уменьшает счетчик комментариев поста.
     *
     * @param postId идентификатор поста
     */
    @Transactional
    void decrementCommentsCount(Long postId);
}
