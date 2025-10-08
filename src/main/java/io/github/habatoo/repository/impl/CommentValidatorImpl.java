package io.github.habatoo.repository.impl;

import io.github.habatoo.dto.request.CommentCreateRequest;
import io.github.habatoo.repository.CommentValidator;
import org.springframework.stereotype.Component;

/**
 * Реализация валидатора для комментариев.
 *
 * <p>Предоставляет методы для проверки корректности данных, связанных с комментариями,
 * включая идентификаторы постов и комментариев, текст комментариев и запросы на создание.</p>
 */
@Component
public class CommentValidatorImpl implements CommentValidator {

    /**
     * {@inheritDoc}
     */
    @Override
    public void validatePostId(Long postId) {
        if (postId == null || postId <= 0) {
            throw new IllegalArgumentException("Post ID must be positive");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateIds(Long postId, Long commentId) {
        validatePostId(postId);
        if (commentId == null || commentId <= 0) {
            throw new IllegalArgumentException("Comment ID must be positive");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateCommentText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment text cannot be empty");
        }
        if (text.length() > 1000) {
            throw new IllegalArgumentException("Comment text too long");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateCommentRequest(CommentCreateRequest request) {
        validatePostId(request.postId());
        validateCommentText(request.text());
    }
}
