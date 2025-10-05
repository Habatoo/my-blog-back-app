package io.github.habatoo.service;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Контракт для обработки изображений
 */
public interface ImageService {

    /**
     * Обновляет изображение для указанного поста
     *
     * @param postId идентификатор поста
     * @param image  файл изображения
     * @throws EmptyResultDataAccessException если пост с указанным ID не найден
     * @throws IllegalArgumentException       если файл изображения невалиден
     * @throws DataAccessException            при ошибках доступа к базе данных
     */
    void updatePostImage(Long postId, MultipartFile image);

    /**
     * Получает изображение поста
     *
     * @param postId идентификатор поста
     * @return массив байт изображения
     * @throws EmptyResultDataAccessException если пост с указанным ID не найден
     * @throws IllegalArgumentException       если у поста нет изображения
     * @throws DataAccessException            при ошибках доступа к базе данных
     */
    byte[] getPostImage(Long postId);

}
