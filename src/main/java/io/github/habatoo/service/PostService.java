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


    PostListResponse getPosts(String search, int pageNumber, int pageSize);

    Optional<PostResponse> getPostById(Long id);

    PostResponse createPost(PostCreateRequest postCreateRequest);

    PostResponse updatePost(PostRequest postRequest);

    void deletePost(Long id);

    int incrementLikes(Long id);

    void incrementCommentsCount(Long id);

    void decrementCommentsCount(Long id);

    boolean postExists(Long postId);

}
