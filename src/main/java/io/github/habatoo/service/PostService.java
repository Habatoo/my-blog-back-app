package io.github.habatoo.service;

import io.github.habatoo.controller.dto.PostRequest;
import io.github.habatoo.model.Post;
import io.github.habatoo.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с постами блога.
 * Предоставляет бизнес-логику для операций с постами.
 *
 * <p>Сервис делегирует выполнение операций доступа к данным репозиторию
 * и не содержит сложной бизнес-логики.</p>
 *
 * @see PostRepository
 * @see Post
 */
@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * Получает все посты из системы.
     *
     * <p>Возвращает полный список постов, отсортированный в порядке,
     * определенном реализацией репозитория. Обычно посты возвращаются
     * в хронологическом порядке от новых к старым.</p>
     *
     * @return список всех постов, содержащихся в системе.
     * Если посты отсутствуют, возвращается пустой список
     * @see PostRepository#findAll()
     */
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    /**
     * Получение поста по идентификатору с полной информацией.
     * Возвращает пост с полным текстом, тегами и количеством комментариев.
     *
     * @param id идентификатор поста
     * @return Optional с постом если найден
     */
    public Optional<Post> getPostById(Long id) {
        return postRepository.findByIdWithFullContent(id);
    }

    /**
     * Создание нового поста с тегами.
     *
     * @param postRequest DTO с данными для создания поста TODO
     * @return DTO с созданным постом
     */
    public Post createPost(PostRequest postRequest) {
        return postRepository.save(postRequest);
    }

    /**
     * Обновление существующего поста
     *
     * @param postRequest DTO с данными для обновления поста
     * @return обновленный пост
     */
    public Post updatePost(PostRequest postRequest) {
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
