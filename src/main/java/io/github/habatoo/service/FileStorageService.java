package io.github.habatoo.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Контракт для обработки файлов
 */
public interface FileStorageService {

    /**
     * Сохраняет файл изображения на диск
     *
     * @param postId идентификатор поста
     * @param file   файл изображения
     * @return относительный путь к сохраненному файлу
     * @throws IOException при ошибках сохранения файла
     */
    String saveImageFile(Long postId, MultipartFile file) throws IOException;

    /**
     * Загружает файл изображения с диска
     *
     * @param filename относительный путь к файлу
     * @return ресурс с содержимым файла
     */
    Resource loadImageFile(String filename);

    /**
     * Удаляет файл изображения с диска
     *
     * @param filename относительный путь к файлу
     */
    void deleteImageFile(String filename);

    /**
     * Удаляет папку поста если она пустая
     *
     * @param postId идентификатор поста
     */
    void deletePostDirectory(Long postId);

    /**
     * Проверяет тип загружаемого изображения.
     *
     * @param image файл изображения
     * @return true если тип изображения поддерживается
     */
    boolean isValidImageType(MultipartFile image);

}
