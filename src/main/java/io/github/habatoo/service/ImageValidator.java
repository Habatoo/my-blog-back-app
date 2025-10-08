package io.github.habatoo.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Контракт для проверки изображений перед обработкой.
 */
public interface ImageValidator {

    /**
     * Валидирует параметры обновления изображения поста.
     *
     * @param postId идентификатор поста для обновления
     * @param image  файл изображения для валидации
     * @throws IllegalArgumentException если любой из параметров невалиден
     */
    void validateImageUpdate(Long postId, MultipartFile image);

    /**
     * Проверяет валидность MIME-типа изображения.
     *
     * @param image файл изображения для проверки
     * @return true если MIME-тип поддерживается, false в противном случае
     */
    boolean isValidImageType(MultipartFile image);

    /**
     * Проверяет валидность расширения файла.
     *
     * @param extension расширение файла для проверки
     * @return true если расширение поддерживается, false в противном случае
     */
    boolean isValidExtension(String extension);

}
