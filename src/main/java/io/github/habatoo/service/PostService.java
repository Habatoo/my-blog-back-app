package io.github.habatoo.service;

import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostListResponse;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.repository.PostRepository;
import org.springframework.stereotype.Service;

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
@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * Получает посты из системы.
     *
     * <p>Возвращает список постов с пагинацией  отфильтрованных по поиску,
     * отсортированный в порядке, определенном реализацией репозитория.</p>
     *
     * @return список постов, содержащихся в системе.
     * Если посты отсутствуют, возвращается пустой список
     * @see PostRepository#findPostsWithPagination
     */
    public PostListResponse getPosts(String search, int pageNumber, int pageSize) {
        return postRepository.findPostsWithPagination(search, pageNumber, pageSize);
    }

    /**
     * Получение поста по идентификатору с полной информацией.
     * Возвращает пост с полным текстом, тегами и количеством комментариев.
     *
     * @param id идентификатор поста
     * @return Optional с постом если найден
     */
    public Optional<PostResponse> getPostById(Long id) {
        return postRepository.findByIdWithFullContent(id);
    }

    /**
     * Создание нового поста с тегами.
     *
     * @param postRequest DTO с данными для создания поста
     * @return DTO с созданным постом
     */
    public PostResponse createPost(PostRequest postRequest) {
        return postRepository.save(postRequest);
    }

    /**
     * Обновление существующего поста
     *
     * @param postRequest DTO с данными для обновления поста
     * @return обновленный пост
     */
    public PostResponse updatePost(PostRequest postRequest) {
        return postRepository.update(postRequest);
    }

    /**
     * Удаляет пост по идентификатору вместе со всеми комментариями и связями с тегами
     *
     * @param id идентификатор поста для удаления
     * @throws RuntimeException если пост с указанным идентификатором не найден
     */
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    /**
     * Увеличивает счетчик лайков поста на 1
     *
     * @param id идентификатор поста для увеличения лайков
     * @return обновленное количество лайков поста
     */
    public int incrementLikes(Long id) {
        return postRepository.incrementLikes(id);
    }
}
