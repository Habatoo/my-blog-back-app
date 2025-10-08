package io.github.habatoo.repository;

import io.github.habatoo.dto.request.PostCreateRequest;
import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.model.Post;
import io.github.habatoo.repository.impl.PostRepositoryImpl;
import io.github.habatoo.service.dto.PostCounters;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс репозитория для работы с постами блога.
 * Определяет контракты для операций доступа к данным постов.
 *
 * <p>Интерфейс расширяет Spring Data {@link Repository} и предоставляет
 * методы для извлечения постов вместе со связанными сущностями.</p>
 *
 * @see Repository
 * @see Post
 * @see PostRepositoryImpl
 */
public interface PostRepository {

    /**
     * Вставляет пост в базу данных и возвращает сгенерированный ID.
     *
     * @param postCreateRequest запрос на создание поста
     * @return сгенерированный идентификатор поста
     */
    Long insertPost(PostCreateRequest postCreateRequest);

    /**
     * Обновляет основные данные поста.
     *
     * @param postRequest запрос на обновление поста
     */
    void updatePostData(PostRequest postRequest);

    /**
     * Получает счетчики лайков и комментариев для поста.
     *
     * @param postId идентификатор поста
     * @return объект с счетчиками
     */
    PostCounters getPostCounters(Long postId);

    /**
     * Получает теги для конкретного поста.
     *
     * @param postId идентификатор поста
     * @return список имен тегов
     */
    List<String> getTagsForPost(Long postId);

    /**
     * Получает имя файла изображения для поста.
     *
     * @param postId идентификатор поста
     * @return имя файла или null если изображение не установлено
     */
    String getImageFileName(Long postId);

    /**
     * Проверяет существование поста.
     *
     * @param postId идентификатор поста
     * @return true если пост существует, false в противном случае
     */
    boolean postExists(Long postId);

    /**
     * Подсчитывает количество постов по поисковому запросу.
     *
     * @param searchPattern шаблон поиска
     * @return количество постов
     */
    Integer countPostsBySearch(String searchPattern);

    /**
     * Находит посты по поисковому запросу с пагинацией.
     *
     * @param searchPattern шаблон поиска
     * @param pageSize      размер страницы
     * @param offset        смещение
     * @return список постов
     */
    List<PostResponse> findPostsBySearchPaginated(String searchPattern, int pageSize, int offset);

    /**
     * Находит пост по идентификатору.
     *
     * @param id идентификатор поста
     * @return Optional с постом или empty если не найден
     */
    Optional<PostResponse> findById(Long id);

    /**
     * Получает количество лайков поста.
     *
     * @param postId идентификатор поста
     * @return количество лайков
     */
    Integer getLikesCount(Long postId);

    /**
     * Увеличивает счетчик лайков.
     *
     * @param postId идентификатор поста
     */
    void incrementLikes(Long postId);

    /**
     * Увеличивает счетчик комментариев.
     *
     * @param postId идентификатор поста
     */
    void incrementCommentsCount(Long postId);

    /**
     * Уменьшает счетчик комментариев.
     *
     * @param postId идентификатор поста
     */
    void decrementCommentsCount(Long postId);

    /**
     * Удаляет пост по идентификатору.
     *
     * @param id идентификатор поста
     * @return количество удаленных строк
     */
    int deleteById(Long id);
}
