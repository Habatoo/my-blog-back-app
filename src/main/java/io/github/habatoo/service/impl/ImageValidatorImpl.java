package io.github.habatoo.service.impl;

import io.github.habatoo.exception.image.InvalidImageException;
import io.github.habatoo.exception.image.InvalidImageTypeException;
import io.github.habatoo.exception.post.PostInvalidException;
import io.github.habatoo.service.ImageValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Валидатор для проверки изображений перед обработкой.
 * Выполняет проверку метаданных изображения.
 */
@Component
public class ImageValidatorImpl implements ImageValidator {

    private final Set<String> allowedContentTypes;
    private final Set<String> allowedExtensions;

    public ImageValidatorImpl(
            @Value("${app.image.allowed-content-types:image/jpeg,image/jpg,image/png}") String contentTypes,
            @Value("${app.image.allowed-extensions:jpg,jpeg,png}") String extensions) {
        this.allowedContentTypes = parseCommaSeparatedValues(contentTypes);
        this.allowedExtensions = parseCommaSeparatedValues(extensions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateImageUpdate(Long postId, MultipartFile image) {
        validatePostId(postId);
        validateImageFile(image);
        validateImageType(image);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidImageType(MultipartFile image) {
        String contentType = image.getContentType();
        return contentType != null && allowedContentTypes.contains(contentType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidExtension(String extension) {
        return extension != null && allowedExtensions.contains(extension.toLowerCase());
    }

    /**
     * Валидирует идентификатор поста.
     *
     * @param postId идентификатор поста для валидации
     * @throws PostInvalidException если идентификатор невалиден
     */
    private void validatePostId(Long postId) {
        if (postId == null || postId <= 0) {
            throw new PostInvalidException();
        }
    }

    /**
     * Валидирует базовые параметры файла изображения.
     *
     * @param image файл изображения для валидации
     * @throws InvalidImageException если файл невалиден
     */
    private void validateImageFile(MultipartFile image) {
        if (image == null) {
            throw new InvalidImageException("Image file cannot be null");
        }
        if (image.isEmpty()) {
            throw new InvalidImageException("Image file cannot be empty");
        }
        if (image.getOriginalFilename() == null || image.getOriginalFilename().trim().isEmpty()) {
            throw new InvalidImageException("Image file name cannot be empty");
        }
    }

    /**
     * Валидирует MIME-тип изображения.
     *
     * @param image файл изображения для проверки типа
     * @throws InvalidImageTypeException если тип изображения не поддерживается
     */
    private void validateImageType(MultipartFile image) {
        if (!isValidImageType(image)) {
            throw new InvalidImageTypeException(
                    String.format("Unsupported image type. Allowed types: %s", allowedContentTypes)
            );
        }
    }

    /**
     * Парсит данные из файла свойств.
     *
     * @param values значения из файла.
     * @return сет значений.
     */
    private Set<String> parseCommaSeparatedValues(String values) {
        return Arrays.stream(values.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toSet());
    }
}
