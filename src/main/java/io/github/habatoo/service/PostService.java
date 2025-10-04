package io.github.habatoo.service;

import io.github.habatoo.model.Post;
import io.github.habatoo.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
