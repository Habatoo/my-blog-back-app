package io.github.habatoo.service.impl;

import io.github.habatoo.dto.request.CommentCreateRequest;
import io.github.habatoo.dto.response.CommentResponse;
import io.github.habatoo.model.Comment;
import io.github.habatoo.repository.CommentRepository;
import io.github.habatoo.service.CommentService;
import org.springframework.stereotype.Service;

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
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostId(postId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CommentResponse> getCommentByPostIdAndId(Long postId, Long commentId) {
        return commentRepository.findByPostIdAndId(postId, commentId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentResponse createComment(CommentCreateRequest commentCreateRequest) {
        return commentRepository.save(commentCreateRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CommentResponse> updateComment(Long postId, Long commentId, String text) {
        return commentRepository.updateTextAndUpdatedAt(postId, commentId, text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteComment(Long postId, Long commentId) {
        return commentRepository.deleteById(postId, commentId);
    }

}
