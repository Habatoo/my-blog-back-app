package io.github.habatoo.repository.impl;

import io.github.habatoo.dto.request.CommentCreateRequest;
import io.github.habatoo.dto.response.CommentResponse;
import io.github.habatoo.repository.CommentRepository;
import io.github.habatoo.repository.mapper.CommentRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Реализация репозитория для работы с комментариями блога.
 * Обеспечивает доступ к данным комментариев с использованием JDBC Template.
 *
 * @see CommentRepository
 * @see JdbcTemplate
 * @see CommentRowMapper
 */
@Repository
public class CommentRepositoryImpl implements CommentRepository {

    private final JdbcTemplate jdbcTemplate;
    private final CommentRowMapper commentRowMapper;

    private static final String FIND_BY_POST_ID = """
            SELECT id, text, post_id
            FROM comment
            WHERE post_id = ?
            ORDER BY created_at ASC
            """;

    private static final String FIND_BY_POST_ID_AND_ID = """
            SELECT id, text, post_id
            FROM comment
            WHERE post_id = ? AND id = ?
            """;

    private static final String INSERT_COMMENT = """
            INSERT INTO comment (post_id, text, created_at, updated_at)
            VALUES (?, ?, ?, ?)
            RETURNING id, text, post_id
            """;

    private static final String UPDATE_COMMENT_TEXT = """
            UPDATE comment
            SET text = ?, updated_at = ?
            WHERE id = ?
            RETURNING id, text, post_id
            """;

    private static final String DELETE_COMMENT = """
            DELETE FROM comment WHERE id = ?
            """;

    public CommentRepositoryImpl(
            JdbcTemplate jdbcTemplate,
            CommentRowMapper commentRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.commentRowMapper = commentRowMapper;
    }

    @Override
    public List<CommentResponse> findByPostId(Long postId) {
        return jdbcTemplate.query(FIND_BY_POST_ID, commentRowMapper, postId);
    }

    @Override
    public Optional<CommentResponse> findByPostIdAndId(Long postId, Long commentId) {
        List<CommentResponse> comments = jdbcTemplate.query(FIND_BY_POST_ID_AND_ID, commentRowMapper, postId, commentId);
        return comments.stream().findFirst();
    }

    @Override
    public CommentResponse save(CommentCreateRequest commentCreateRequest) {
        LocalDateTime now = LocalDateTime.now();

        return jdbcTemplate.queryForObject(
                INSERT_COMMENT,
                (rs, rowNum) -> new CommentResponse(
                        rs.getLong("id"),
                        rs.getString("text"),
                        rs.getLong("post_id")
                ),
                commentCreateRequest.postId(),
                commentCreateRequest.text(),
                Timestamp.valueOf(now),
                Timestamp.valueOf(now)
        );
    }

    @Override
    public CommentResponse updateText(Long commentId, String text) {
        return jdbcTemplate.queryForObject(
                UPDATE_COMMENT_TEXT,
                (rs, rowNum) -> new CommentResponse(
                        rs.getLong("id"),
                        rs.getString("text"),
                        rs.getLong("post_id")
                ),
                text,
                Timestamp.valueOf(LocalDateTime.now()),
                commentId
        );
    }

    @Override
    public int deleteById(Long commentId) {
        return jdbcTemplate.update(DELETE_COMMENT, commentId);
    }
}
