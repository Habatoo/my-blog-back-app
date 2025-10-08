package io.github.habatoo.service;

import io.github.habatoo.dto.request.PostCreateRequest;
import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostListResponse;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.repository.PostRepository;

import java.util.Optional;

/**
 * Сервис для работы с постами блога.
 * Предоставляет бизнес-логику для операций с постами.
 *
 * <p>Сервис делегирует выполнение операций доступа к данным репозиторию
 * и не содержит сложной бизнес-логики.</p>
 *
 * @see PostRepository
 * @see PostResponse
 */
public interface PostService {

    /**
     * Получает посты из системы.
     *
     * <p>Возвращает список постов с пагинацией  отфильтрованных по поиску,
     * отсортированный в порядке, определенном реализацией репозитория.</p>
     *
     * @return список постов, содержащихся в системе.
     * Если посты отсутствуют, возвращается пустой список
     * @see PostRepository
     */
    PostListResponse getPosts(String search, int pageNumber, int pageSize);

    /**
     * Получение поста по идентификатору с полной информацией.
     * Возвращает пост с полным текстом, тегами и количеством комментариев.
     *
     * @param id идентификатор поста
     * @return Optional с постом если найден
     */
    Optional<PostResponse> getPostById(Long id);

    /**
     * Создание нового поста с тегами.
     *
     * @param postCreateRequest DTO с данными для создания поста
     * @return DTO с созданным постом
     */
    PostResponse createPost(PostCreateRequest postCreateRequest);

    /**
     * Обновление существующего поста
     *
     * @param postRequest DTO с данными для обновления поста
     * @return обновленный пост
     */
    PostResponse updatePost(PostRequest postRequest);

    /**
     * Удаляет пост по идентификатору вместе со всеми комментариями и связями с тегами
     *
     * @param id идентификатор поста для удаления
     * @throws RuntimeException если пост с указанным идентификатором не найден
     */
    void deletePost(Long id);

    /**
     * Увеличивает счетчик лайков поста на 1
     *
     * @param id идентификатор поста для увеличения лайков
     * @return обновленное количество лайков поста
     */
    int incrementLikes(Long id);

    /**
     * Увеличивает счетчик комментариев поста.
     *
     * @param id идентификатор поста
     */
    void incrementCommentsCount(Long id);

    /**
     * Уменьшает счетчик комментариев поста.
     *
     * @param id идентификатор поста
     */
    void decrementCommentsCount(Long id);
}
