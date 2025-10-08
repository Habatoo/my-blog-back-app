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

    public ImageRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> findImageFileNameByPostId(Long postId) {
        validatePostId(postId);

        try {
            String fileName = jdbcTemplate.queryForObject(
                    "SELECT image_url FROM post WHERE id = ?",
                    String.class,
                    postId
            );
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
        validateUpdateParameters(postId, fileName, originalName, size);

        int updatedRows = jdbcTemplate.update(
                "UPDATE post SET image_name = ?, image_size = ?, image_url = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                originalName,
                size,
                fileName,
                postId
        );

        if (updatedRows == 0) {
            throw new EmptyResultDataAccessException("Post not found with id: " + postId, 1);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsPostById(Long postId) {
        validatePostId(postId);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post WHERE id = ?",
                Integer.class,
                postId
        );
        return count != null && count > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public int deleteImageMetadata(Long postId) {
        validatePostId(postId);

        return jdbcTemplate.update(
                "UPDATE post SET image_name = NULL, image_size = NULL, image_url = NULL, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                postId
        );
    }

    /**
     * Валидирует идентификатор поста.
     *
     * @param postId идентификатор поста для валидации
     * @throws IllegalArgumentException если идентификатор невалиден
     */
    private void validatePostId(Long postId) {
        if (postId == null || postId <= 0) {
            throw new IllegalArgumentException("Post ID must be positive: " + postId);
        }
    }

    /**
     * Валидирует параметры для обновления метаданных изображения.
     *
     * @param postId       идентификатор поста
     * @param fileName     имя файла
     * @param originalName оригинальное имя
     * @param size         размер файла
     * @throws IllegalArgumentException если любой параметр невалиден
     */
    private void validateUpdateParameters(Long postId, String fileName, String originalName, long size) {
        validatePostId(postId);

        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        if (originalName == null || originalName.trim().isEmpty()) {
            throw new IllegalArgumentException("Original file name cannot be null or empty");
        }
        if (size < 0) {
            throw new IllegalArgumentException("File size cannot be negative: " + size);
        }
    }
}