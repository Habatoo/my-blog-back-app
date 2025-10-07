package io.github.habatoo.service.impl;

import io.github.habatoo.repository.ImageRepository;
import io.github.habatoo.service.ImageService;
import io.github.habatoo.service.dto.ImageResponse;
import org.springframework.http.MediaType;
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
    public ImageResponse getPostImage(Long postId) {
        byte[] imageData = imageRepository.getPostImage(postId);
        String contentType = determineContentType(imageData);
        MediaType mediaType = obtainMediaType(contentType);

        return new ImageResponse(imageData, mediaType);
    }

    /**
     * Определяет Content-Type для изображения на основе его данных.
     *
     * <p>Анализирует массив байт изображения для определения его формата.
     * В реальной реализации может использовать библиотеки для определения
     * формата изображения по magic numbers.</p>
     *
     * @param imageData массив байт изображения
     * @return строку с MIME-типом изображения
     */
    private String determineContentType(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        if (imageData.length >= 3 &&
                imageData[0] == (byte) 0xFF &&
                imageData[1] == (byte) 0xD8 &&
                imageData[2] == (byte) 0xFF) {
            return "image/jpeg";
        }

        if (imageData.length >= 4 &&
                imageData[0] == (byte) 0x89 &&
                imageData[1] == (byte) 0x50 &&
                imageData[2] == (byte) 0x4E &&
                imageData[3] == (byte) 0x47) {
            return "image/png";
        }

        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    private MediaType obtainMediaType(String contentType) {
        return MediaType.parseMediaType(contentType);
    }

}
