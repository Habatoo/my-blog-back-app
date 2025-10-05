package io.github.habatoo.repository.impl;

import io.github.habatoo.repository.ImageRepository;
import io.github.habatoo.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Repository
public class ImageRepositoryImpl implements ImageRepository {

    private final JdbcTemplate jdbcTemplate;
    private final FileStorageService fileStorageService;

    public ImageRepositoryImpl(JdbcTemplate jdbcTemplate, FileStorageService fileStorageService) {
        this.jdbcTemplate = jdbcTemplate;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Обновляет изображение поста в базе данных
     *
     * @param postId идентификатор поста
     * @param image  файл изображения
     * @throws EmptyResultDataAccessException если пост с указанным ID не найден
     * @throws IllegalArgumentException       если файл изображения невалиден
     * @throws DataAccessException            при ошибках доступа к базе данных
     */
    @Override
    @Transactional
    public void updatePostImage(Long postId, MultipartFile image) {
        if (postId == null) {
            throw new IllegalArgumentException("Post ID cannot be null");
        }
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image file cannot be null or empty");
        }
        if (!fileStorageService.isValidImageType(image)) {
            throw new IllegalArgumentException("Invalid image type. Supported types: JPEG, PNG, GIF");
        }

        Integer postExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post WHERE id = ?",
                Integer.class,
                postId
        );

        if (postExists == null || postExists == 0) {
            throw new EmptyResultDataAccessException("Post with id " + postId + " not found", 1);
        }

        try {
            String oldFileName = getCurrentImageFileName(postId);

            String savedFileName = fileStorageService.saveImageFile(postId, image);

            int updatedRows = jdbcTemplate.update(
                    "UPDATE post SET image_name = ?, image_size = ?, image_url = ?, updated_at = ? WHERE id = ?",
                    image.getOriginalFilename(),
                    (int) image.getSize(),
                    savedFileName,
                    LocalDateTime.now(),
                    postId
            );

            if (updatedRows == 0) {
                throw new DataAccessException("Failed to update post image") {
                };
            }

            if (oldFileName != null) {
                fileStorageService.deleteImageFile(oldFileName);
            }

        } catch (IOException e) {
            throw new DataAccessException("Error saving image file: " + e.getMessage(), e) {
            };
        } catch (Exception e) {
            throw new DataAccessException("Error updating post image: " + e.getMessage(), e) {
            };
        }
    }

    /**
     * Получает изображение поста
     *
     * @param postId идентификатор поста
     * @return массив байт изображения
     * @throws EmptyResultDataAccessException если пост с указанным ID не найден или нет изображения
     * @throws DataAccessException            при ошибках доступа к базе данных
     */
    @Override
    public byte[] getPostImage(Long postId) {
        String imageFileName = getCurrentImageFileName(postId);
        if (imageFileName == null) {
            throw new EmptyResultDataAccessException("Post with id " + postId + " has no image", 1);
        }

        try {
            Resource imageResource = fileStorageService.loadImageFile(imageFileName);
            return imageResource.getContentAsByteArray();
        } catch (IOException e) {
            throw new DataAccessException("Error reading image file: " + e.getMessage(), e) {
            };
        }
    }

    /**
     * Получает текущее имя файла изображения для поста
     *
     * @param postId идентификатор поста
     * @return имя файла или null если изображение не установлено
     */
    private String getCurrentImageFileName(Long postId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT image_url FROM post WHERE id = ?",
                    String.class,
                    postId
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

}
