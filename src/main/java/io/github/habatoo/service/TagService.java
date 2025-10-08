package io.github.habatoo.service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Контракт для работы с тегами
 */
public interface TagService {

    /**
     * Обрабатывает теги поста: создает или находит существующие теги и создает связи.
     *
     * @param postId   идентификатор поста
     * @param tagNames список имен тегов
     * @return список имен тегов для ответа
     */
    @Transactional
    List<String> processPostTags(Long postId, List<String> tagNames);
}
