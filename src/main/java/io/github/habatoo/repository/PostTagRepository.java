package io.github.habatoo.repository;

import io.github.habatoo.model.PostTag;
import org.springframework.data.repository.Repository;

/**
 * Репозиторий для работы со связями постов и тегов
 */
public interface PostTagRepository extends Repository<PostTag, Long> {

    /**
     * Создание связи между постом и тегом
     *
     * @param postTag связь поста с тегом
     */
    void save(PostTag postTag);
}
