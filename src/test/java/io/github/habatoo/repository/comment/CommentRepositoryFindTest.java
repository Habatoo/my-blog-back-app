package io.github.habatoo.repository.comment;

import io.github.habatoo.dto.response.CommentResponse;
import io.github.habatoo.repository.mapper.CommentRowMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Тесты методов findByPostId и findByPostIdAndId
 */
@DisplayName("Тесты методов поиска комментариев")
class CommentRepositoryFindTest extends CommentRepositoryTestBase {

    @Test
    @DisplayName("Должен вернуть список комментариев для заданного postId")
    void shouldReturnCommentsByPostId() {
        List<CommentResponse> expectedComments = List.of(
                createCommentResponse(COMMENT_ID, POST_ID, COMMENT_TEXT)
        );
        final String FIND_BY_POST_ID = """
                SELECT id, text, post_id
                FROM comment
                WHERE post_id = ?
                ORDER BY created_at ASC
                """;

        when(jdbcTemplate.query(anyString(), any(CommentRowMapper.class), eq(POST_ID)))
                .thenReturn(expectedComments);

        List<CommentResponse> result = commentRepository.findByPostId(POST_ID);

        assertEquals(expectedComments, result);
        verify(jdbcTemplate).query(FIND_BY_POST_ID, commentRowMapper, POST_ID);
    }

    @Test
    @DisplayName("Должен вернуть Optional с комментарием при существовании по postId и commentId")
    void shouldReturnOptionalCommentByPostIdAndIdFound() {
        List<CommentResponse> comments = List.of(createCommentResponse(COMMENT_ID, POST_ID, COMMENT_TEXT));
        final String FIND_BY_POST_ID_AND_ID = """
                SELECT id, text, post_id
                FROM comment
                WHERE post_id = ? AND id = ?
                """;

        when(jdbcTemplate.query(anyString(), any(CommentRowMapper.class), eq(POST_ID), eq(COMMENT_ID)))
                .thenReturn(comments);

        Optional<CommentResponse> result = commentRepository.findByPostIdAndId(POST_ID, COMMENT_ID);

        assertTrue(result.isPresent());
        assertEquals(COMMENT_ID, result.get().id());
        verify(jdbcTemplate).query(FIND_BY_POST_ID_AND_ID, commentRowMapper, POST_ID, COMMENT_ID);
    }

    @Test
    @DisplayName("Должен вернуть пустой Optional если комментарий не найден")
    void shouldReturnEmptyOptionalIfCommentNotFound() {
        when(jdbcTemplate.query(anyString(), any(CommentRowMapper.class), eq(POST_ID), eq(COMMENT_ID)))
                .thenReturn(Collections.emptyList());

        Optional<CommentResponse> result = commentRepository.findByPostIdAndId(POST_ID, COMMENT_ID);

        assertTrue(result.isEmpty());
    }
}
