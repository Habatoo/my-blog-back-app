package io.github.habatoo.repository.image;

import io.github.habatoo.repository.ImageRepository;
import io.github.habatoo.repository.impl.ImageRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Класс для тестирования ImageRepositoryImpl.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты методов ImageRepository.")
class ImageRepositoryImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private ImageRepository imageRepository;

    private final Long existingPostId = 1L;
    private final Long nonExistingPostId = 999L;
    private final String imageName = "image_file.jpg";
    private static final String GET_IMAGE_FILE_NAME = """
            SELECT image_url
            FROM post
            WHERE id = ?
            """;
    private static final String UPDATE_POST_IMAGE = """
            UPDATE post
            SET image_name = ?, image_size = ?, image_url = ?
            WHERE id = ?
            """;
    private static final String CHECK_POST_EXISTS = """
            SELECT COUNT(1)
            FROM post
            WHERE id = ?
            """;


    @BeforeEach
    void setUp() {
        imageRepository = new ImageRepositoryImpl(jdbcTemplate);
    }

    @Test
    @DisplayName("Должен вернуть имя файла изображения, если пост существует")
    void shouldReturnImageFileNameIfPostExists() {
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), eq(existingPostId))).thenReturn(imageName);

        Optional<String> result = imageRepository.findImageFileNameByPostId(existingPostId);

        assertTrue(result.isPresent());
        assertEquals(imageName, result.get());

        verify(jdbcTemplate).queryForObject(GET_IMAGE_FILE_NAME, String.class, existingPostId);
    }

    @Test
    @DisplayName("Должен вернуть пустой Optional, если пост не найден")
    void shouldReturnEmptyOptionalIfPostNotFound() {
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), eq(nonExistingPostId)))
                .thenThrow(new EmptyResultDataAccessException(1));

        Optional<String> result = imageRepository.findImageFileNameByPostId(nonExistingPostId);

        assertTrue(result.isEmpty());
        verify(jdbcTemplate).queryForObject(GET_IMAGE_FILE_NAME, String.class, nonExistingPostId);
    }

    @Test
    @DisplayName("Должен обновить метаданные изображения поста при успешном обновлении")
    void shouldUpdateImageMetadataSuccessfully() {
        when(jdbcTemplate.update(anyString(), anyString(), anyLong(), anyString(), anyLong())).thenReturn(1);

        assertDoesNotThrow(() -> imageRepository.updateImageMetadata(existingPostId, imageName, "original.jpg", 12345L));

        verify(jdbcTemplate).update(UPDATE_POST_IMAGE, "original.jpg", 12345L, imageName, existingPostId);
    }

    @Test
    @DisplayName("Должен выбросить исключение при обновлении метаданных изображения несуществующего поста")
    void shouldThrowWhenUpdateImageMetadataFails() {
        when(jdbcTemplate.update(anyString(), anyString(), anyLong(), anyString(), anyLong())).thenReturn(0);

        EmptyResultDataAccessException ex = assertThrows(EmptyResultDataAccessException.class,
                () -> imageRepository.updateImageMetadata(existingPostId, imageName, "original.jpg", 12345L));

        assertTrue(ex.getMessage().contains("Post not found with id"));
        verify(jdbcTemplate).update(UPDATE_POST_IMAGE, "original.jpg", 12345L, imageName, existingPostId);
    }

    @Test
    @DisplayName("Должен вернуть true если пост с заданным id существует")
    void shouldReturnTrueIfPostExists() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(existingPostId))).thenReturn(1);

        assertTrue(imageRepository.existsPostById(existingPostId));
        verify(jdbcTemplate).queryForObject(CHECK_POST_EXISTS, Integer.class, existingPostId);
    }

    @Test
    @DisplayName("Должен вернуть false если пост с заданным id не существует")
    void shouldReturnFalseIfPostDoesNotExist() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(nonExistingPostId))).thenReturn(0);

        assertFalse(imageRepository.existsPostById(nonExistingPostId));
        verify(jdbcTemplate).queryForObject(CHECK_POST_EXISTS, Integer.class, nonExistingPostId);
    }

    @Test
    @DisplayName("Должен выбрасывать EmptyResultDataAccessException если updateImageMetadata обновляет 0 строк")
    void shouldThrowEmptyResultDataAccessExceptionWhenNoRowsUpdated() {
        Long postId = 123L;
        String fileName = "file.jpg";
        String originalName = "original.jpg";
        long size = 100L;

        when(jdbcTemplate.update(anyString(), anyString(), anyLong(), anyString(), anyLong())).thenReturn(0);

        EmptyResultDataAccessException ex = assertThrows(EmptyResultDataAccessException.class, () ->
                imageRepository.updateImageMetadata(postId, fileName, originalName, size));

        assertTrue(ex.getMessage().contains("Post not found with id"));
        verify(jdbcTemplate).update(UPDATE_POST_IMAGE, originalName, size, fileName, postId);
    }
}
