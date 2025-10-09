package io.github.habatoo.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Контракт для проверки изображений перед обработкой.
 */
public interface ImageValidator {

    void validateImageUpdate(Long postId, MultipartFile image);

    void validatePostId(Long postId);

}
