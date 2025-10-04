package io.github.habatoo.repository;

import io.github.habatoo.model.Post;
import org.springframework.data.repository.Repository;

import java.util.List;

/**
 * Репозиторий для работы с постами блога.
 * Определяет контракты для операций доступа к данным постов.
 *
 * <p>Интерфейс расширяет Spring Data {@link Repository} и предоставляет
 * методы для извлечения постов вместе со связанными сущностями.</p>
 *
 * @see Repository
 * @see Post
 * @see PostRepositoryImpl
 */
public interface PostRepository extends Repository<Post, Long> {

    /**
     * Получение всех постов с тегами и комментариями одним запросом.
     *
     * @return список всех постов
     */
    List<Post> findAll();
}
