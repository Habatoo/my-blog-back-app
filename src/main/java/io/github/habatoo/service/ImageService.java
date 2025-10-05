package io.github.habatoo.service;

import io.github.habatoo.repository.ImageRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {

    private final ImageRepository imageRepository;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    /**
     * Обновляет изображение для указанного поста
     *
     * @param postId идентификатор поста
     * @param image  файл изображения
     * @throws EmptyResultDataAccessException если пост с указанным ID не найден
     * @throws IllegalArgumentException       если файл изображения невалиден
     * @throws DataAccessException            при ошибках доступа к базе данных
     */
    public void updatePostImage(Long postId, MultipartFile image) {
        imageRepository.updatePostImage(postId, image);
    }

    /**
     * Получает изображение поста
     *
     * @param postId идентификатор поста
     * @return массив байт изображения
     * @throws EmptyResultDataAccessException если пост с указанным ID не найден
     * @throws IllegalArgumentException       если у поста нет изображения
     * @throws DataAccessException            при ошибках доступа к базе данных
     */
    public byte[] getPostImage(Long postId) {
        return imageRepository.getPostImage(postId);
    }

}
