package io.github.habatoo.service.comment;

import io.github.habatoo.dto.request.CommentCreateRequest;
import io.github.habatoo.dto.response.CommentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тесты метода createComment класса CommentServiceImpl
 */
@DisplayName("Тесты метода createComment")
class CommentServiceCreateCommentTest extends CommentServiceTestBase {

    @Test
    @DisplayName("Должен создать комментарий и обновить кеш и счетчик комментариев поста")
    void shouldCreateCommentAndUpdateCacheAndPost() {
        when(postService.postExists(VALID_POST_ID)).thenReturn(true);

        CommentCreateRequest request = createCommentCreateRequest(COMMENT_TEXT, VALID_POST_ID);
        CommentResponse savedComment = createCommentResponse(VALID_COMMENT_ID, VALID_POST_ID, COMMENT_TEXT);

        when(commentRepository.save(request)).thenReturn(savedComment);

        CommentResponse result = commentService.createComment(request);

        assertEquals(savedComment, result);
        verify(postService).postExists(VALID_POST_ID);
        verify(commentRepository).save(request);
        verify(postService).incrementCommentsCount(VALID_POST_ID);

        List<CommentResponse> cachedComments = commentService.getCommentsByPostId(VALID_POST_ID);
        assertTrue(cachedComments.contains(savedComment));
    }

    @Test
    @DisplayName("Должен выбрасывать исключение, если пост не существует")
    void shouldThrowIfPostDoesNotExist() {
        when(postService.postExists(INVALID_POST_ID)).thenReturn(false);

        CommentCreateRequest request = createCommentCreateRequest(COMMENT_TEXT, INVALID_POST_ID);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> commentService.createComment(request));
        assertTrue(ex.getMessage().contains("Post not found"));

        verify(postService).postExists(INVALID_POST_ID);
        verifyNoInteractions(commentRepository);
    }
}
