package io.github.habatoo.service.postservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

/**
 * Тесты методов incrementLikes, incrementCommentsCount и decrementCommentsCount класса PostServiceImpl
 */
@DisplayName("Тесты методов подсчёта лайков и комментариев")
class PostServiceCountMethodsTest extends PostServiceTestBase {

    @Test
    @DisplayName("Должен увеличить лайки успешно")
    void shouldIncrementLikes() {
        doNothing().when(postRepository).incrementLikes(VALID_POST_ID);

        int newLikes = postService.incrementLikes(VALID_POST_ID);

        assertEquals(POST_RESPONSE_1.likesCount() + 1, newLikes);
        verify(postRepository).incrementLikes(VALID_POST_ID);
    }

    @Test
    @DisplayName("Должен бросать исключение при увеличении лайков несуществующего поста")
    void shouldThrowWhenIncrementLikesNonexistent() {
        doNothing().when(postRepository).incrementLikes(INVALID_POST_ID);

        assertThrows(IllegalStateException.class, () -> postService.incrementLikes(INVALID_POST_ID));
    }

    @ParameterizedTest(name = "Метод: {0}")
    @ValueSource(strings = {"incrementCommentsCount", "decrementCommentsCount"})
    @DisplayName("Должен корректно работать методы инкремента и декремента комментариев")
    void shouldHandleCommentsCountChanges(String methodName) {
        if ("incrementCommentsCount".equals(methodName)) {
            postService.incrementCommentsCount(VALID_POST_ID);
            assertEquals(POST_RESPONSE_1.commentsCount() + 1,
                    postService.getPostById(VALID_POST_ID).get().commentsCount());
            verify(postRepository).incrementCommentsCount(VALID_POST_ID);
        } else {
            postService.decrementCommentsCount(VALID_POST_ID);
            int expected = Math.max(0, POST_RESPONSE_1.commentsCount() - 1);
            assertEquals(expected, postService.getPostById(VALID_POST_ID).get().commentsCount());
            verify(postRepository).decrementCommentsCount(VALID_POST_ID);
        }
    }
}
