package io.github.habatoo.service.impl;

import io.github.habatoo.dto.request.CommentCreateRequest;
import io.github.habatoo.dto.response.CommentResponse;
import io.github.habatoo.exception.post.PostNotFoundException;
import io.github.habatoo.model.Comment;
import io.github.habatoo.repository.CommentRepository;
import io.github.habatoo.repository.impl.CommentValidatorImpl;
import io.github.habatoo.service.CommentService;
import io.github.habatoo.service.PostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с комментариями блога.
 * Предоставляет бизнес-логику для операций с комментариями к постам.
 *
 * <p>Сервис делегирует выполнение операций доступа к данным репозиторию
 * и обеспечивает работу с комментариями, связанных с конкретными постами.</p>
 *
 * @see CommentRepository
 * @see Comment
 */
@Service
@Transactional
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final PostService postService;
    private final CommentValidatorImpl commentValidator;

    public CommentServiceImpl(
            CommentRepository commentRepository,
            PostService postService,
            CommentValidatorImpl commentValidator) {
        this.commentRepository = commentRepository;
        this.postService = postService;
        this.commentValidator = commentValidator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        commentValidator.validatePostId(postId);
        if (!commentRepository.existsPostById(postId)) {
            throw new PostNotFoundException(postId);
        }
        return commentRepository.findByPostId(postId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CommentResponse> getCommentByPostIdAndId(Long postId, Long commentId) {
        commentValidator.validateIds(postId, commentId);
        return commentRepository.findByPostIdAndId(postId, commentId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentResponse createComment(CommentCreateRequest request) {
        commentValidator.validateCommentRequest(request);

        if (!commentRepository.existsPostById(request.postId())) {
            throw new PostNotFoundException(request.postId());
        }

        Long commentId = commentRepository.save(request);
        postService.incrementCommentsCount(request.postId());

        return new CommentResponse(commentId, request.text(), request.postId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CommentResponse> updateComment(Long postId, Long commentId, String text) {
        commentValidator.validateIds(postId, commentId);
        commentValidator.validateCommentText(text);

        if (!commentRepository.existsByIdAndPostId(commentId, postId)) {
            return Optional.empty();
        }

        int updated = commentRepository.updateText(commentId, text);
        if (updated == 0) {
            return Optional.empty();
        }

        return commentRepository.findByPostIdAndId(postId, commentId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteComment(Long postId, Long commentId) {
        commentValidator.validateIds(postId, commentId);

        if (!commentRepository.existsByIdAndPostId(commentId, postId)) {
            return false;
        }

        int deleted = commentRepository.deleteById(commentId);
        if (deleted > 0) {
            postService.decrementCommentsCount(postId);
            return true;
        }
        return false;
    }
}
