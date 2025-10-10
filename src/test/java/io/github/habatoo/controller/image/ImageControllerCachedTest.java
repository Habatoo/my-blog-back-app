package io.github.habatoo.controller.image;

import io.github.habatoo.controller.ImageController;
import io.github.habatoo.handler.GlobalExceptionHandler;
import io.github.habatoo.service.ImageService;
import io.github.habatoo.service.dto.ImageResponse;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Тесты для ImageController с максимальным кешированием MockMvc
 * Использует Standalone Setup с @BeforeAll для однократной инициализации
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Тесты unit уровня методов контроллера ImageController с использованием Cached MockMvc.")
class ImageControllerCachedTest {

    private MockMvc mockMvc;
    private ImageService imageService;

    private MockMultipartFile mockImageFile;
    private ImageResponse mockImageResponse;

    @BeforeAll
    void setUpAll() {
        imageService = mock(ImageService.class);
        ImageController imageController = new ImageController(imageService);
        mockMvc = MockMvcBuilders.standaloneSetup(imageController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        initializeTestData();
    }

    /**
     * Инициализация тестовых данных
     */
    private void initializeTestData() {
        byte[] imageData = "fake image data".getBytes();
        mockImageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                imageData
        );

        mockImageResponse = new ImageResponse(
                imageData,
                MediaType.IMAGE_JPEG
        );
    }

    @BeforeEach
    void setUp() {
        reset(imageService);
    }

    /**
     * Тест успешного обновления изображения поста
     */
    @Test
    @DisplayName("PUT /api/posts/{postId}/image - должен обновить изображение поста")
    void updatePostImage_WithValidImage_ShouldReturnOk() throws Exception {
        Long postId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/posts/{postId}/image", postId)
                        .file(mockImageFile)
                        .with(request -> {
                            request.setMethod("PUT"); // Меняем метод на PUT для multipart
                            return request;
                        }))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(imageService, times(1)).updatePostImage(postId, mockImageFile);
    }

    /**
     * Тест обновления изображения с несуществующим постом
     */
    @Test
    @DisplayName("PUT /api/posts/{postId}/image - должен вернуть ошибку для несуществующего поста")
    void updatePostImage_WithNonExistentPost_ShouldThrowException() throws Exception {
        Long postId = 999L;
        doThrow(new org.springframework.dao.EmptyResultDataAccessException("Post not found", 1))
                .when(imageService).updatePostImage(anyLong(), any());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/posts/{postId}/image", postId)
                        .file(mockImageFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        verify(imageService, times(1)).updatePostImage(postId, mockImageFile);
    }

    /**
     * Тест обновления изображения с невалидным файлом
     */
    @Test
    @DisplayName("PUT /api/posts/{postId}/image - должен вернуть ошибку для невалидного файла")
    void updatePostImage_WithInvalidFile_ShouldReturnBadRequest() throws Exception {
        Long postId = 1L;
        doThrow(new IllegalArgumentException("Invalid image file"))
                .when(imageService).updatePostImage(anyLong(), any());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/posts/{postId}/image", postId)
                        .file(mockImageFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verify(imageService, times(1)).updatePostImage(postId, mockImageFile);
    }

    /**
     * Тест успешного получения изображения поста
     */
    @Test
    @DisplayName("GET /api/posts/{postId}/image - должен вернуть изображение")
    void getPostImage_WithValidPostId_ShouldReturnImage() throws Exception {
        Long postId = 1L;
        when(imageService.getPostImage(postId)).thenReturn(mockImageResponse);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/{postId}/image", postId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        assertEquals(MediaType.IMAGE_JPEG_VALUE, result.getResponse().getContentType());
        assertNotNull(result.getResponse().getContentAsByteArray());
        assertEquals("fake image data", new String(result.getResponse().getContentAsByteArray()));

        verify(imageService, times(1)).getPostImage(postId);
    }

    /**
     * Тест получения изображения для несуществующего поста
     */
    @Test
    @DisplayName("GET /api/posts/{postId}/image - должен вернуть 404 для несуществующего поста")
    void getPostImage_WithNonExistentPost_ShouldReturnNotFound() throws Exception {
        Long postId = 999L;
        when(imageService.getPostImage(postId))
                .thenThrow(new org.springframework.dao.EmptyResultDataAccessException("Post not found", 1));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/{postId}/image", postId))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        verify(imageService, times(1)).getPostImage(postId);
    }

    /**
     * Тест получения изображения для поста без изображения
     */
    @Test
    @DisplayName("GET /api/posts/{postId}/image - должен вернуть 404 если изображение не найдено")
    void getPostImage_WithNoImage_ShouldReturnNotFound() throws Exception {
        Long postId = 2L;
        when(imageService.getPostImage(postId))
                .thenThrow(new org.springframework.dao.EmptyResultDataAccessException("Image not found", 1));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/{postId}/image", postId))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        verify(imageService, times(1)).getPostImage(postId);
    }

    /**
     * Тест получения изображения с разными форматами
     */
    @Test
    @DisplayName("GET /api/posts/{postId}/image - должен работать с разными типами изображений")
    void getPostImage_WithDifferentImageTypes_ShouldWorkCorrectly() throws Exception {
        Long[] postIds = {1L, 2L, 3L};
        MediaType[] mediaTypes = {
                MediaType.IMAGE_JPEG,
                MediaType.IMAGE_PNG,
                MediaType.IMAGE_GIF
        };
        String[] imageData = {
                "jpeg image data",
                "png image data",
                "gif image data"
        };

        for (int i = 0; i < postIds.length; i++) {
            reset(imageService);
            Long postId = postIds[i];
            ImageResponse imageResponse = new ImageResponse(
                    imageData[i].getBytes(),
                    mediaTypes[i]
            );

            when(imageService.getPostImage(postId)).thenReturn(imageResponse);

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/{postId}/image", postId))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn();

            assertEquals(mediaTypes[i].toString(), result.getResponse().getContentType());
            assertEquals(imageData[i], new String(result.getResponse().getContentAsByteArray()));

            verify(imageService, times(1)).getPostImage(postId);
        }
    }

    /**
     * Тест обработки ошибки доступа к данным
     */
    @Test
    @DisplayName("GET /api/posts/{postId}/image - должен вернуть 500 при ошибке БД")
    void getPostImage_WithDataAccessError_ShouldReturnInternalServerError() throws Exception {
        Long postId = 1L;
        when(imageService.getPostImage(postId))
                .thenThrow(new org.springframework.dao.DataAccessException("Database error") {
                });

        mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/{postId}/image", postId))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());

        verify(imageService, times(1)).getPostImage(postId);
    }

    /**
     * Тест с пустым файлом изображения
     */
    @Test
    @DisplayName("PUT /api/posts/{postId}/image - должен вернуть ошибку для пустого файла")
    void updatePostImage_WithEmptyFile_ShouldReturnBadRequest() throws Exception {
        Long postId = 1L;
        MockMultipartFile emptyFile = new MockMultipartFile(
                "image",
                "empty.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0] // Пустой файл
        );
        doThrow(new IllegalArgumentException("Image file cannot be empty"))
                .when(imageService).updatePostImage(anyLong(), any());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/posts/{postId}/image", postId)
                        .file(emptyFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verify(imageService, times(1)).updatePostImage(postId, emptyFile);
    }
}
