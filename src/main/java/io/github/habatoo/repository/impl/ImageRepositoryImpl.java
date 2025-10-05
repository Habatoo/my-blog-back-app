package io.github.habatoo.repository.impl;

import io.github.habatoo.repository.ImageRepository;
import io.github.habatoo.repository.impl.util.ImageOperationHandler;
import io.github.habatoo.repository.impl.util.ImageValidationUtils;
import io.github.habatoo.service.FileStorageService;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Реализация репозитория для работы с изображениями постов.
 * Обеспечивает обновление и получение изображений, связанных с постами.
 */
@Repository
public class ImageRepositoryImpl implements ImageRepository {

    private final ImageOperationHandler imageOperationHandler;
    private final FileStorageService fileStorageService;

    public ImageRepositoryImpl(JdbcTemplate jdbcTemplate, FileStorageService fileStorageService) {
        this.imageOperationHandler = new ImageOperationHandler(jdbcTemplate, fileStorageService);
        this.fileStorageService = fileStorageService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updatePostImage(Long postId, MultipartFile image) {
        ImageValidationUtils.validateImageUpdateParams(postId, image);

        if (!fileStorageService.isValidImageType(image)) {
            throw new IllegalArgumentException("Invalid image type. Supported types: JPEG, PNG, GIF");
        }

        imageOperationHandler.validatePostExists(postId);

        try {
            imageOperationHandler.handleImageUpdate(postId, image);
        } catch (IOException e) {
            throw new DataAccessException("Error saving image file: " + e.getMessage(), e) {
            };
        } catch (Exception e) {
            throw new DataAccessException("Error updating post image: " + e.getMessage(), e) {
            };
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getPostImage(Long postId) {
        ImageValidationUtils.validateImageRetrievalParams(postId);

        try {
            return imageOperationHandler.handleImageRetrieval(postId);
        } catch (IOException e) {
            throw new DataAccessException("Error reading image file: " + e.getMessage(), e) {
            };
        }

    }

}