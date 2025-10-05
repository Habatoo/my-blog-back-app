package io.github.habatoo.repository.impl;

import io.github.habatoo.model.PostTag;
import io.github.habatoo.repository.PostTagRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

import static io.github.habatoo.repository.sql.PostTagSqlQueries.INSERT_INTO_POST_TAG;

/**
 * Реализация репозитория для работы со связями постов и тегов
 */
@Repository
public class PostTagRepositoryImpl implements PostTagRepository {

    private final JdbcTemplate jdbcTemplate;

    public PostTagRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(PostTag postTag) {
        jdbcTemplate.update(
                INSERT_INTO_POST_TAG,
                postTag.getPostId(),
                postTag.getTagId(),
                Timestamp.valueOf(postTag.getCreatedAt())
        );
    }
}
