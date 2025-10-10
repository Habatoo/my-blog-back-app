package io.github.habatoo.repository.comment;

import io.github.habatoo.dto.request.CommentCreateRequest;
import io.github.habatoo.dto.response.CommentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Тесты методов save, updateText и deleteById
 */
@DisplayName("Тесты методов сохранения, обновления и удаления комментариев")
class CommentRepositoryModifyTest extends CommentRepositoryTestBase {

    @Test
    @DisplayName("Должен сохранить новый комментарий и вернуть созданный объект")
    void shouldSaveNewComment() {
        CommentCreateRequest createRequest = createCommentCreateRequest(COMMENT_TEXT, POST_ID);
        CommentResponse expectedResponse = createCommentResponse(COMMENT_ID, POST_ID, COMMENT_TEXT);
        final String INSERT_COMMENT = """
                INSERT INTO comment (post_id, text, created_at, updated_at)
                VALUES (?, ?, ?, ?)
                RETURNING id, text, post_id
                """;

        when(jdbcTemplate.queryForObject(
                eq(INSERT_COMMENT),
                any(RowMapper.class),
                eq(POST_ID),
                eq(COMMENT_TEXT),
                any(Timestamp.class),
                any(Timestamp.class)))
                .thenReturn(expectedResponse);

        CommentResponse result = commentRepository.save(createRequest);

        assertEquals(expectedResponse, result);
        verify(jdbcTemplate).queryForObject(anyString(), any(RowMapper.class),
                eq(POST_ID), eq(COMMENT_TEXT), any(Timestamp.class), any(Timestamp.class));
    }

    @Test
    @DisplayName("Должен обновить текст комментария и вернуть обновленный объект")
    void shouldUpdateCommentText() {
        CommentResponse expectedResponse = createCommentResponse(COMMENT_ID, POST_ID, "Updated Text");
        final String UPDATE_COMMENT_TEXT = """
                UPDATE comment
                SET text = ?, updated_at = ?
                WHERE id = ?
                RETURNING id, text, post_id
                """;

        when(jdbcTemplate.queryForObject(
                eq(UPDATE_COMMENT_TEXT),
                any(RowMapper.class),
                eq("Updated Text"),
                any(Timestamp.class),
                eq(COMMENT_ID)))
                .thenReturn(expectedResponse);

        CommentResponse result = commentRepository.updateText(COMMENT_ID, "Updated Text");

        assertEquals(expectedResponse, result);
        verify(jdbcTemplate).queryForObject(anyString(), any(RowMapper.class),
                eq("Updated Text"), any(Timestamp.class), eq(COMMENT_ID));
    }

    @Test
    @DisplayName("Должен удалить комментарий по id и вернуть количество удаленных записей")
    void shouldDeleteCommentById() {
        final String DELETE_COMMENT = """
                DELETE FROM comment WHERE id = ?
                """;
        when(jdbcTemplate.update(eq(DELETE_COMMENT), eq(COMMENT_ID))).thenReturn(1);

        int deleted = commentRepository.deleteById(COMMENT_ID);

        assertEquals(1, deleted);
        verify(jdbcTemplate).update(DELETE_COMMENT, COMMENT_ID);
    }
}
