package io.github.habatoo.repository.impl;

import io.github.habatoo.dto.request.CommentCreateRequest;
import io.github.habatoo.dto.response.CommentResponse;
import io.github.habatoo.repository.CommentRepository;
import io.github.habatoo.repository.mapper.CommentRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static io.github.habatoo.repository.sql.CommentSqlQueries.*;

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

    public CommentRepositoryImpl(
            JdbcTemplate jdbcTemplate,
            CommentRowMapper commentRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.commentRowMapper = commentRowMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CommentResponse> findByPostId(Long postId) {
        return jdbcTemplate.query(FIND_BY_POST_ID, commentRowMapper, postId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CommentResponse> findByPostIdAndId(Long postId, Long commentId) {
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
    @Override
    public Long save(CommentCreateRequest commentCreateRequest) {
        Long postId = commentCreateRequest.postId();
        String text = commentCreateRequest.text();
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_COMMENT, new String[]{"id"});
            ps.setLong(1, postId);
            ps.setString(2, text);
            LocalDateTime now = LocalDateTime.now();
            ps.setTimestamp(3, Timestamp.valueOf(now));
            ps.setTimestamp(4, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int updateText(Long commentId, String text) {
        return jdbcTemplate.update(UPDATE_COMMENT_TEXT, text, LocalDateTime.now(), commentId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int deleteById(Long commentId) {
        return jdbcTemplate.update(DELETE_COMMENT, commentId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByIdAndPostId(Long commentId, Long postId) {
        Integer count = jdbcTemplate.queryForObject(COUNT_COMMENT_EXISTS, Integer.class, commentId, postId);
        return count != null && count > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsPostById(Long postId) {
        Integer count = jdbcTemplate.queryForObject(COUNT_POST_EXISTS, Integer.class, postId);
        return count != null && count > 0;
    }
}