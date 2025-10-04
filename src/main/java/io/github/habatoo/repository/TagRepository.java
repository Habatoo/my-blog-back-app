package io.github.habatoo.repository;

import io.github.habatoo.model.Tag;
import org.springframework.data.repository.Repository;
import java.util.Optional;

/**
 * Репозиторий для работы с тегами
 */
public interface TagRepository extends Repository<Tag, Long> {

    /**
     * Поиск тега по имени
     * @param name имя тега
     * @return Optional с тегом, если найден
     */
    Optional<Tag> findByName(String name);

    /**
     * Сохранение тега
     * @param tagName имя тега для сохранения
     * @return сохраненный тег
     */
    Tag save(String tagName);
}
