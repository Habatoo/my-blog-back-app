package io.github.habatoo.service.impl;

import io.github.habatoo.dto.request.PostCreateRequest;
import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostListResponse;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.repository.PostRepository;
import io.github.habatoo.service.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с постами блога.
 * Предоставляет бизнес-логику для операций с постами.
 *
 * @see PostRepository
 * @see PostResponse
 */
@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostBusinessService postBusinessService;
    private final PostStatisticsService statisticsService;
    private final PaginationService paginationService;
    private final PostValidationService validationService;

    /**
     * Конструктор сервиса постов.
     *
     * @param postRepository        репозиторий постов
     * @param postBusinessService   сервис бизнес-операций
     * @param statisticsService     сервис статистики
     * @param paginationService     сервис пагинации
     * @param validationService     сервис валидации
     */
    public PostServiceImpl(PostRepository postRepository,
                           PostBusinessService postBusinessService,
                           PostStatisticsService statisticsService,
                           PaginationService paginationService,
                           PostValidationService validationService) {
        this.postRepository = postRepository;
        this.postBusinessService = postBusinessService;
        this.statisticsService = statisticsService;
        this.paginationService = paginationService;
        this.validationService = validationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostListResponse getPosts(String search, int pageNumber, int pageSize) {
        validationService.validatePaginationParams(search, pageNumber, pageSize);

        int offset = (pageNumber - 1) * pageSize;

        Integer totalCount = postRepository.countPostsBySearch(search);

        if (totalCount == null || totalCount == 0) {
            return new PostListResponse(List.of(), false, false, 0);
        }

        List<PostResponse> posts = postRepository.findPostsBySearchPaginated(search, pageSize, offset);

        List<PostResponse> postsWithTags = enrichPostsWithTags(posts);

        return paginationService.createPostListResponse(
                postsWithTags, totalCount, pageNumber, pageSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<PostResponse> getPostById(Long id) {
        validationService.validatePostId(id);

        Optional<PostResponse> post = postRepository.findById(id);
        return post.map(this::enrichWithTags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostResponse createPost(PostCreateRequest postCreateRequest) {
        return postBusinessService.createPostWithTags(postCreateRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostResponse updatePost(PostRequest postRequest) {
        return postBusinessService.updatePostWithTags(postRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePost(Long id) {
        postBusinessService.deletePostWithCleanup(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int incrementLikes(Long id) {
        return statisticsService.incrementLikes(id);
    }

    /**
     * Увеличивает счетчик комментариев поста.
     *
     * @param id идентификатор поста
     */
    @Override
    public void incrementCommentsCount(Long id) {
        statisticsService.incrementCommentsCount(id);
    }

    /**
     * Уменьшает счетчик комментариев поста.
     *
     * @param id идентификатор поста
     */
    @Override
    public void decrementCommentsCount(Long id) {
        statisticsService.decrementCommentsCount(id);
    }

    /**
     * Обогащает пост тегами.
     *
     * @param post пост для обогащения
     * @return пост с тегами
     */
    private PostResponse enrichWithTags(PostResponse post) {
        List<String> tags = postRepository.getTagsForPost(post.id());
        return new PostResponse(
                post.id(),
                post.title(),
                post.text(),
                tags,
                post.likesCount(),
                post.commentsCount()
        );
    }

    /**
     * Обогащает список постов тегами.
     *
     * @param posts список постов для обогащения
     * @return список постов с тегами
     */
    private List<PostResponse> enrichPostsWithTags(List<PostResponse> posts) {
        return posts.stream()
                .map(this::enrichWithTags)
                .toList();
    }
}