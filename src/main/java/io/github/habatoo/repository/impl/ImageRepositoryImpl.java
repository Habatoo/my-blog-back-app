package io.github.habatoo.repository.impl;

import io.github.habatoo.repository.ImageRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Реализация репозитория для работы с метаданными изображений постов.
 *
 * <p>Использует JdbcTemplate для выполнения SQL-запросов к базе данных.
 * Предоставляет доступ к метаданным изображений, хранящимся в таблице постов.</p>
 */
@Repository
public class ImageRepositoryImpl implements ImageRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String GET_IMAGE_FILE_NAME = """
            SELECT image_url
            FROM post
            WHERE id = ?
            """;

    private static final String UPDATE_POST_IMAGE = """
            UPDATE post
            SET image_name = ?, image_size = ?, image_url = ?
            WHERE id = ?
            """;

    private static final String CHECK_POST_EXISTS = """
            SELECT COUNT(1)
            FROM post
            WHERE id = ?
            """;

    public ImageRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> findImageFileNameByPostId(Long postId) {
        try {
            String fileName = jdbcTemplate.queryForObject(GET_IMAGE_FILE_NAME, String.class, postId);
            return Optional.ofNullable(fileName);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateImageMetadata(Long postId, String fileName, String originalName, long size) {
        int updatedRows = jdbcTemplate.update(UPDATE_POST_IMAGE, originalName, size, fileName, postId);
        if (updatedRows == 0) {
            throw new EmptyResultDataAccessException("Post not found with id: " + postId, 1);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsPostById(Long postId) {
        Integer count = jdbcTemplate.queryForObject(CHECK_POST_EXISTS, Integer.class, postId);
        return count != null && count > 0;
    }
}
