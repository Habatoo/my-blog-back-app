package io.github.habatoo.service.imageservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Тесты метода updatePostImage класса ImageServiceImpl
 */
@DisplayName("Тесты метода updatePostImage")
class ImageServiceUpdatePostImageTest extends ImageServiceTestBase {

    @Test
    @DisplayName("Должен корректно обновить изображение поста при валидных данных")
    void shouldUpdatePostImageSuccessfully() throws IOException {
        MultipartFile imageFile = createMultipartFile(false, ORIGINAL_FILENAME, IMAGE_SIZE);

        doNothing().when(imageValidator).validatePostId(VALID_POST_ID);
        doNothing().when(imageValidator).validateImageUpdate(VALID_POST_ID, imageFile);
        when(imageRepository.existsPostById(VALID_POST_ID)).thenReturn(true);
        when(imageRepository.findImageFileNameByPostId(VALID_POST_ID)).thenReturn(Optional.of(IMAGE_FILENAME));
        when(fileStorageService.saveImageFile(VALID_POST_ID, imageFile)).thenReturn(IMAGE_FILENAME);
        doNothing().when(imageRepository).updateImageMetadata(VALID_POST_ID, IMAGE_FILENAME, ORIGINAL_FILENAME, IMAGE_SIZE);
        doNothing().when(fileStorageService).deleteImageFile(IMAGE_FILENAME);
        when(fileStorageService.loadImageFile(IMAGE_FILENAME)).thenReturn(IMAGE_DATA);
        when(contentTypeDetector.detect(IMAGE_DATA)).thenReturn(MEDIA_TYPE);

        imageService.updatePostImage(VALID_POST_ID, imageFile);

        verify(imageValidator).validatePostId(VALID_POST_ID);
        verify(imageValidator).validateImageUpdate(VALID_POST_ID, imageFile);
        verify(imageRepository).existsPostById(VALID_POST_ID);
        verify(imageRepository).findImageFileNameByPostId(VALID_POST_ID);
        verify(fileStorageService).saveImageFile(VALID_POST_ID, imageFile);
        verify(imageRepository).updateImageMetadata(VALID_POST_ID, IMAGE_FILENAME, ORIGINAL_FILENAME, IMAGE_SIZE);
        verify(fileStorageService).deleteImageFile(IMAGE_FILENAME);
        verify(fileStorageService).loadImageFile(IMAGE_FILENAME);
        verify(contentTypeDetector).detect(IMAGE_DATA);
    }

    @Test
    @DisplayName("Должен выбросить исключение если пост не найден")
    void shouldThrowIfPostNotFound() {
        MultipartFile imageFile = createMultipartFile(false, ORIGINAL_FILENAME, IMAGE_SIZE);

        doNothing().when(imageValidator).validatePostId(INVALID_POST_ID);

        doNothing().when(imageValidator).validateImageUpdate(INVALID_POST_ID, imageFile);
        when(imageRepository.existsPostById(INVALID_POST_ID)).thenReturn(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> imageService.updatePostImage(INVALID_POST_ID, imageFile));

        assertTrue(ex.getMessage().contains("Post not found with id"));

        verify(imageValidator).validatePostId(INVALID_POST_ID);
        verify(imageValidator).validateImageUpdate(INVALID_POST_ID, imageFile);
        verify(imageRepository).existsPostById(INVALID_POST_ID);
        verifyNoMoreInteractions(fileStorageService);
    }

    @Test
    @DisplayName("Должен выбросить исключение если произошла ошибка при работе с файлом")
    void shouldThrowWhenIOExceptionOccurs() throws IOException {
        MultipartFile imageFile = createMultipartFile(false, ORIGINAL_FILENAME, IMAGE_SIZE);

        doNothing().when(imageValidator).validatePostId(VALID_POST_ID);
        doNothing().when(imageValidator).validateImageUpdate(VALID_POST_ID, imageFile);
        when(imageRepository.existsPostById(VALID_POST_ID)).thenReturn(true);
        when(imageRepository.findImageFileNameByPostId(VALID_POST_ID)).thenReturn(Optional.of(IMAGE_FILENAME));
        when(fileStorageService.saveImageFile(VALID_POST_ID, imageFile)).thenThrow(new IOException("IO error"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> imageService.updatePostImage(VALID_POST_ID, imageFile));

        assertTrue(ex.getMessage().contains("Failed to process image file"));

        verify(imageValidator).validatePostId(VALID_POST_ID);
        verify(imageValidator).validateImageUpdate(VALID_POST_ID, imageFile);
        verify(imageRepository).existsPostById(VALID_POST_ID);
        verify(imageRepository).findImageFileNameByPostId(VALID_POST_ID);
        verify(fileStorageService).saveImageFile(VALID_POST_ID, imageFile);
    }
}
