package io.github.habatoo.service.impl;

import io.github.habatoo.repository.PostRepository;
import io.github.habatoo.service.PostStatisticsService;
import org.springframework.stereotype.Service;

/**
 * Сервис для работы со статистикой постов
 */
@Service
public class PostStatisticsServiceImpl implements PostStatisticsService {

    private final PostRepository postRepository;
    private final PostValidationServiceImpl validationService;

    public PostStatisticsServiceImpl(PostRepository postRepository,
                                     PostValidationServiceImpl validationService) {
        this.postRepository = postRepository;
        this.validationService = validationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int incrementLikes(Long postId) {
        validationService.validatePostId(postId);
        validationService.validatePostExists(postId);

        postRepository.incrementLikes(postId);
        Integer likesCount = postRepository.getLikesCount(postId);

        if (likesCount == null) {
            throw new IllegalStateException("Likes count is null for post with id " + postId);
        }

        return likesCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incrementCommentsCount(Long postId) {
        validationService.validatePostId(postId);
        validationService.validatePostExists(postId);
        postRepository.incrementCommentsCount(postId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void decrementCommentsCount(Long postId) {
        validationService.validatePostId(postId);
        validationService.validatePostExists(postId);
        postRepository.decrementCommentsCount(postId);
    }
}
