package io.github.habatoo.service.impl;

import io.github.habatoo.dto.request.PostCreateRequest;
import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostListResponse;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.repository.PostRepository;
import io.github.habatoo.service.PostService;
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
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * {@inheritDoc}
     */
    public PostListResponse getPosts(String search, int pageNumber, int pageSize) {
        return postRepository.findPostsWithPagination(search, pageNumber, pageSize);
    }

    /**
     * {@inheritDoc}
     */
    public Optional<PostResponse> getPostById(Long id) {
        return postRepository.findByIdWithFullContent(id);
    }

    /**
     * {@inheritDoc}
     */
    public PostResponse createPost(PostCreateRequest postCreateRequest) {
        return postRepository.save(postCreateRequest);
    }

    /**
     * {@inheritDoc}
     */
    public PostResponse updatePost(PostRequest postRequest) {
        return postRepository.update(postRequest);
    }

    /**
     * {@inheritDoc}
     */
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    public int incrementLikes(Long id) {
        return postRepository.incrementLikes(id);
    }
}
