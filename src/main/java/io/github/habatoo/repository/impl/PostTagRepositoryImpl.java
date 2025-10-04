package io.github.habatoo.repository.impl;

import io.github.habatoo.model.PostTag;
import io.github.habatoo.repository.PostTagRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

/**
 * Реализация репозитория для работы со связями постов и тегов
 */
@Repository
public class PostTagRepositoryImpl implements PostTagRepository {

    private final JdbcTemplate jdbcTemplate;

    public PostTagRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(PostTag postTag) {
        jdbcTemplate.update(
                "INSERT INTO post_tag (post_id, tag_id, created_at) VALUES (?, ?, ?)",
                postTag.getPostId(),
                postTag.getTagId(),
                Timestamp.valueOf(postTag.getCreatedAt())
        );
    }
}
