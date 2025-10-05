package io.github.habatoo.service.impl;

import io.github.habatoo.repository.ImageRepository;
import io.github.habatoo.service.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Сервис для обработки изображений поста.
 */
@Service
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;

    public ImageServiceImpl(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    /**
     * {@inheritDoc}
     */
    public void updatePostImage(Long postId, MultipartFile image) {
        imageRepository.updatePostImage(postId, image);
    }

    /**
     * {@inheritDoc}
     */
    public byte[] getPostImage(Long postId) {
        return imageRepository.getPostImage(postId);
    }

}
