package io.github.habatoo.repository;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Репозиторий для работы с изображениями постов
 */
public interface ImageRepository {

    /**
     * Обновляет изображение поста в базе данных
     *
     * @param postId идентификатор поста
     * @param image  файл изображения
     * @throws EmptyResultDataAccessException если пост с указанным ID не найден
     * @throws IllegalArgumentException       если файл изображения невалиден
     * @throws DataAccessException            при ошибках доступа к базе данныхх
     */
    void updatePostImage(Long postId, MultipartFile image);

    /**
     * Получает изображение поста
     *
     * @param postId идентификатор поста
     * @return массив байт изображения
     * @throws EmptyResultDataAccessException если пост с указанным ID не найден или нет изображения
     * @throws DataAccessException            при ошибках доступа к базе данных
     */
    byte[] getPostImage(Long postId);
}
