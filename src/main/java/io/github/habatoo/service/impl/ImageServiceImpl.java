package io.github.habatoo.service.impl;

import io.github.habatoo.exception.image.ImageNotFoundException;
import io.github.habatoo.exception.image.ImageStorageException;
import io.github.habatoo.exception.post.PostNotFoundException;
import io.github.habatoo.repository.ImageRepository;
import io.github.habatoo.service.FileStorageService;
import io.github.habatoo.service.ImageService;
import io.github.habatoo.service.dto.ImageResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Сервис для обработки изображений поста.
 */
@Service
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final FileStorageService fileStorageService;
    private final ImageValidatorImpl imageValidator;
    private final ImageContentTypeDetectorImpl contentTypeDetector;

    public ImageServiceImpl(
            ImageRepository imageRepository,
            FileStorageService fileStorageService,
            ImageValidatorImpl imageValidator,
            ImageContentTypeDetectorImpl contentTypeDetector) {
        this.imageRepository = imageRepository;
        this.fileStorageService = fileStorageService;
        this.imageValidator = imageValidator;
        this.contentTypeDetector = contentTypeDetector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePostImage(Long postId, MultipartFile image) {
        imageValidator.validateImageUpdate(postId, image);
        imageValidator.validatePostId(postId);

        if (!imageRepository.existsPostById(postId)) {
            throw new PostNotFoundException(postId);
        }

        String oldFileName = imageRepository.findImageFileNameByPostId(postId)
                .orElse(null);

        try {
            String newFileName = fileStorageService.saveImageFile(postId, image);

            imageValidator.validateUpdateParameters(
                    postId,
                    newFileName,
                    image.getOriginalFilename(),
                    image.getSize());
            imageRepository.updateImageMetadata(
                    postId,
                    newFileName,
                    image.getOriginalFilename(),
                    image.getSize());

            if (oldFileName != null) {
                fileStorageService.deleteImageFile(oldFileName);
            }

        } catch (IOException e) {
            throw new ImageStorageException("Failed to save image", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImageResponse getPostImage(Long postId) {
        imageValidator.validatePostId(postId);
        if (!imageRepository.existsPostById(postId)) {
            throw new PostNotFoundException(postId);
        }

        String fileName = imageRepository.findImageFileNameByPostId(postId)
                .orElseThrow(() -> new ImageNotFoundException(postId));

        try {
            byte[] imageData = fileStorageService.loadImageFile(fileName);

            MediaType mediaType = contentTypeDetector.detect(imageData);

            return new ImageResponse(imageData, mediaType);

        } catch (IOException e) {
            throw new ImageStorageException("Failed to load image", e);
        }
    }

}
