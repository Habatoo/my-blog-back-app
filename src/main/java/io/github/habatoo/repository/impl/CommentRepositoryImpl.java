package io.github.habatoo.repository.impl;

import io.github.habatoo.dto.request.CommentCreateRequest;
import io.github.habatoo.dto.response.CommentResponse;
import io.github.habatoo.repository.CommentRepository;
import io.github.habatoo.repository.mapper.RowMappers;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static io.github.habatoo.repository.sql.CommentSqlQueries.*;
import static io.github.habatoo.repository.impl.util.CommentUtils.*;

/**
 * Реализация репозитория для работы с комментариями блога.
 * Обеспечивает доступ к данным комментариев с использованием JDBC Template.
 *
 * @see CommentRepository
 * @see JdbcTemplate
 */
@Repository
public class CommentRepositoryImpl implements CommentRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMappers.CommentRowMapper commentRowMapper;

    public CommentRepositoryImpl(
            JdbcTemplate jdbcTemplate,
            RowMappers.CommentRowMapper commentRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.commentRowMapper = commentRowMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CommentResponse> findByPostId(Long postId) {
        if (postId == null) {
            throw new IllegalArgumentException("Post ID cannot be null");
        }

        return jdbcTemplate.query(FIND_BY_POST_ID, commentRowMapper, postId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CommentResponse> findByPostIdAndId(Long postId, Long commentId) {
        validateIds(postId, commentId);

        List<CommentResponse> comments = jdbcTemplate.query(
                FIND_BY_POST_ID_AND_ID,
                commentRowMapper,
                postId,
                commentId
        );
        return comments.stream().findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public CommentResponse save(CommentCreateRequest commentCreateRequest) {
        Long postId = commentCreateRequest.postId();
        String text = commentCreateRequest.text();

        if (!postExists(jdbcTemplate, postId)) {
            throw new EmptyResultDataAccessException("Post with id " + postId + " not found", 1);
        }

        Long commentId = insertComment(jdbcTemplate, postId, text);
        updatePostCommentsCount(jdbcTemplate, postId, 1);

        return new CommentResponse(commentId, text, postId);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public Optional<CommentResponse> updateTextAndUpdatedAt(Long postId, Long commentId, String text) {
        validateIds(postId, commentId);
        validateCommentText(text);

        if (!commentExists(jdbcTemplate, postId, commentId)) {
            return Optional.empty();
        }

        int updatedRows = jdbcTemplate.update(
                UPDATE_COMMENT_TEXT,
                text,
                Timestamp.valueOf(LocalDateTime.now()),
                commentId,
                postId
        );

        if (updatedRows == 0) {
            return Optional.empty();
        }

        return Optional.of(new CommentResponse(commentId, text, postId));
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public boolean deleteById(Long postId, Long commentId) {
        validateIds(postId, commentId);

        if (!commentExists(jdbcTemplate, postId, commentId)) {
            throw new EmptyResultDataAccessException("Comment not found", 1);
        }

        int deletedRows = jdbcTemplate.update(DELETE_COMMENT, commentId, postId);

        if (deletedRows > 0) {
            updatePostCommentsCount(jdbcTemplate, postId, -1);
            return true;
        }

        return false;
    }
}