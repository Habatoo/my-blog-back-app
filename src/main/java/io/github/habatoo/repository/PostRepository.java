package io.github.habatoo.repository;

import io.github.habatoo.controller.dto.PostRequest;
import io.github.habatoo.model.Post;
import io.github.habatoo.repository.impl.PostRepositoryImpl;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

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
     * Получение всех постов с тегами и комментариями одним запросом. TODO мсправить под умный поиск
     *
     * @return список всех постов
     */
    List<Post> findAll();

    /**
     * Поиск поста по идентификатору с полной информацией включая теги и комментарии.
     * Возвращает полный текст поста без обрезки.
     *
     * @param id идентификатор поста для поиска
     * @return Optional с постом если найден, иначе пустой Optional
     */
    Optional<Post> findByIdWithFullContent(Long id);

    /**
     * Сохранение нового поста
     *
     * @param post пост для сохранения
     * @return сохраненный пост с присвоенным ID
     */
    Post save(PostRequest post);

    /**
     * Обновление существующего поста
     *
     * @param postRequest обновленные данные для поста
     * @return обновленный пост
     */
    Post update(PostRequest postRequest);

    /**
     * Удаляет пост по идентификатору
     *
     * @param id идентификатор поста для удаления
     */
    void deleteById(Long id);

    /**
     * Увеличивает счетчик лайков поста на 1
     *
     * @param id идентификатор поста
     * @return обновленное количество лайков
     */
    int incrementLikes(Long id);

}
