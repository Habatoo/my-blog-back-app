package io.github.habatoo.service.postservice;

import io.github.habatoo.dto.response.PostResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тесты метода postExists класса PostServiceImpl
 */
@DisplayName("Тесты метода postExists")
class PostServicePostExistsTest extends PostServiceTestBase {

    @ParameterizedTest(name = "Пост с id {0} существует? {1}")
    @MethodSource("postIdProvider")
    void shouldReturnCorrectExistence(Long postId, boolean exists) {
//        if (exists) {
//            postService.postCache.put(postId, POST_RESPONSE_1);
//        } else {
//            postService.postCache.remove(postId);
//        }

        boolean result = postService.postExists(postId);
        assertEquals(exists, result);
    }
}
