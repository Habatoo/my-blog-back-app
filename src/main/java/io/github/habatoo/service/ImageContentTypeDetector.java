package io.github.habatoo.service;

import org.springframework.http.MediaType;

/**
 * Контракт типа контента изображения на основе анализа содержимого файла.
 */
public interface ImageContentTypeDetector {

    /**
     * Определяет MediaType изображения на основе анализа его содержимого.
     *
     * @param imageData массив байт содержимого изображения
     * @return соответствующий MediaType или APPLICATION_OCTET_STREAM если формат не распознан
     * @throws IllegalStateException если imageData равен null
     */
    MediaType detect(byte[] imageData);

}
