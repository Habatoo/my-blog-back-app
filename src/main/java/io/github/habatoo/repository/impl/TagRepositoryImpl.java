package io.github.habatoo.repository.impl;

import io.github.habatoo.model.Tag;
import io.github.habatoo.repository.TagRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.github.habatoo.repository.sql.TagSqlQueries.INSERT_INTO_TAG;
import static io.github.habatoo.repository.sql.TagSqlQueries.SELECT_FROM_TAG;

/**
 * Реализация репозитория для работы с тегами
 */
@Repository
public class TagRepositoryImpl implements TagRepository {

    private final JdbcTemplate jdbcTemplate;

    public TagRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Tag> findByName(String name) {
        List<Tag> tags = jdbcTemplate.query(
                SELECT_FROM_TAG,
                (rs, rowNum) -> Tag.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .build(),
                name
        );
        return tags.stream().findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tag save(String tagName) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_INTO_TAG, new String[]{"id"});
            ps.setString(1, tagName);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, keyHolder);

        Long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();

        return Tag.builder()
                .id(generatedId)
                .name(tagName)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
