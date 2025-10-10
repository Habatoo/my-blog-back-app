package io.github.habatoo.controller.post;

import io.github.habatoo.dto.response.PostResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты обработки получения поста по id.
 */
@DisplayName("Тесты метода getPostById для обработки получения поста по id.")
class PostControllerGetPostByIdTest extends PostControllerTestBase {

    @DisplayName("Должен вернуть пост когда он существует")
    @Test
    void shouldReturnPostWhenExists() {
        PostResponse expectedPost = createPostResponse(VALID_POST_ID, POST_TITLE, POST_TEXT,
                POST_TAGS, 5, 3);

        when(postService.getPostById(VALID_POST_ID)).thenReturn(Optional.of(expectedPost));

        ResponseEntity<PostResponse> response = postController.getPostById(VALID_POST_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedPost, response.getBody());
        verify(postService).getPostById(VALID_POST_ID);
    }

    @DisplayName("Должен вернуть 404 когда пост не найден")
    @Test
    void shouldReturnNotFoundWhenPostDoesNotExist() {
        when(postService.getPostById(NON_EXISTENT_POST_ID)).thenReturn(Optional.empty());

        ResponseEntity<PostResponse> response = postController.getPostById(NON_EXISTENT_POST_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.hasBody());
        verify(postService).getPostById(NON_EXISTENT_POST_ID);
    }

    @DisplayName("Должен корректно обработать различные идентификаторы постов")
    @ParameterizedTest
    @ValueSource(longs = {1L, 10L, 100L, 1000L, Long.MAX_VALUE})
    void shouldHandleDifferentPostIds(Long postId) {
        PostResponse expectedPost = createPostResponse(postId, "Пост " + postId,
                "Текст поста", List.of("tag"), 0, 0);

        when(postService.getPostById(postId)).thenReturn(Optional.of(expectedPost));

        ResponseEntity<PostResponse> response = postController.getPostById(postId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(postId, response.getBody().id());
        verify(postService).getPostById(postId);
    }

}
