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

/**
 * Реализация репозитория для работы с тегами
 */
@Repository
public class TagRepositoryImpl implements TagRepository {

    private final JdbcTemplate jdbcTemplate;

    public TagRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Tag> findByName(String name) {
        List<Tag> tags = jdbcTemplate.query(
                "SELECT id, name, created_at FROM tag WHERE name = ?",
                (rs, rowNum) -> Tag.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .build(),
                name
        );
        return tags.stream().findFirst();
    }

    @Override
    public Tag save(String tagName) {
        final String sql = "INSERT INTO tag (name, created_at) VALUES (?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, tagName);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, keyHolder);

        Long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();

        // Создаем и возвращаем сформированный тег
        return Tag.builder()
                .id(generatedId)
                .name(tagName)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
