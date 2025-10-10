package io.github.habatoo.repository.post;

import io.github.habatoo.dto.request.PostCreateRequest;
import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.repository.PostRepository;
import io.github.habatoo.repository.impl.PostRepositoryImpl;
import io.github.habatoo.repository.mapper.PostListRowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Класс для тестирования репозитория PostRepositoryImpl.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты методов репозитория PostRepositoryImpl для проверки извлечения постов вместе со связанными сущностями.")
class PostRepositoryImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private PostListRowMapper postListRowMapper;

    private PostRepository postRepository;

    private final Long POST_ID = 1L;
    private final Long NON_EXISTING_POST_ID = 999L;

    private final PostResponse examplePost = new PostResponse(
            POST_ID, "Title", "Text", List.of("tag1", "tag2"), 5, 10);

    private static final String FIND_ALL_POSTS = """
            SELECT p.id, p.title, p.text, p.likes_count, p.comments_count
            FROM post p
            ORDER BY p.created_at DESC
            """;

    private static final String CREATE_POST = """
            INSERT INTO post (title, text, likes_count, comments_count, created_at, updated_at)
            VALUES (?, ?, 0, 0, ?, ?)
            RETURNING id, title, text, likes_count, comments_count
            """;

    private static final String INSERT_INTO_TAG = """
            INSERT INTO tag (name)
            VALUES (?)
            ON CONFLICT (name) DO NOTHING
            """;

    private static final String INSERT_INTO_POST_TAG = """
            INSERT INTO post_tag (post_id, tag_id)
            VALUES (?, (SELECT id FROM tag WHERE name = ?))
            ON CONFLICT DO NOTHING
            """;

    private static final String UPDATE_POST = """
            UPDATE post
            SET title = ?, text = ?, updated_at = ?
            WHERE id = ?
            RETURNING id, title, text, likes_count, comments_count
            """;

    private static final String DELETE_POST = """
            DELETE FROM post WHERE id = ?
            """;

    private static final String GET_TAGS_FOR_POST = """
            SELECT t.name FROM tag t
            JOIN post_tag pt ON t.id = pt.tag_id
            WHERE pt.post_id = ?
            """;

    private static final String INCREMENT_LIKES = """
            UPDATE post SET likes_count = likes_count + 1 WHERE id = ?
            """;

    private static final String INCREMENT_COMMENTS_COUNT = """
            UPDATE post SET comments_count = comments_count + 1 WHERE id = ?
            """;

    private static final String DECREMENT_COMMENTS_COUNT = """
            UPDATE post
            SET comments_count = CASE WHEN comments_count > 0 THEN comments_count - 1 ELSE 0 END
            WHERE id = ?
            """;

    @BeforeEach
    void setUp() {
        postRepository = new PostRepositoryImpl(jdbcTemplate, postListRowMapper);
    }

    @Test
    @DisplayName("Должен вернуть список всех постов с тегами")
    void shouldFindAllPostsWithTags() {
        List<PostResponse> postsWithoutTags = List.of(
                new PostResponse(1L, "Title1", "Text1", List.of(), 0, 0),
                new PostResponse(2L, "Title2", "Text2", List.of(), 0, 0)
        );
        when(jdbcTemplate.query(FIND_ALL_POSTS, postListRowMapper)).thenReturn(postsWithoutTags);
        when(jdbcTemplate.query(eq(GET_TAGS_FOR_POST),
                any(RowMapper.class), anyLong())).thenReturn(List.of("tagA", "tagB"));

        List<PostResponse> result = postRepository.findAllPosts();

        assertEquals(postsWithoutTags.size(), result.size());
        for (PostResponse post : result) {
            assertEquals(List.of("tagA", "tagB"), post.tags());
        }

        verify(jdbcTemplate).query(FIND_ALL_POSTS, postListRowMapper);
    }

    @Test
    @DisplayName("Должен создать пост с тегами")
    void shouldCreatePostWithTags() {
        PostCreateRequest createRequest = new PostCreateRequest("New Title", "New Text", List.of("t1", "t2"));

        PostResponse createdPost = new PostResponse(POST_ID, createRequest.title(), createRequest.text(), List.of(), 0, 0);

        when(jdbcTemplate.queryForObject(
                eq(CREATE_POST),
                any(RowMapper.class),
                eq(createRequest.title()),
                eq(createRequest.text()),
                any(Timestamp.class),
                any(Timestamp.class))
        ).thenReturn(createdPost);

        PostResponse result = postRepository.createPost(createRequest);

        assertEquals(POST_ID, result.id());
        assertEquals(createRequest.title(), result.title());
        assertEquals(createRequest.text(), result.text());
        assertEquals(List.of("t1", "t2"), result.tags());

        verify(jdbcTemplate).queryForObject(eq(CREATE_POST), any(RowMapper.class), any(), any(), any(), any());
        verify(jdbcTemplate, times(1)).batchUpdate(contains(INSERT_INTO_TAG), anyList(), anyInt(), any());
        verify(jdbcTemplate, times(1)).batchUpdate(contains(INSERT_INTO_POST_TAG), anyList(), anyInt(), any());
    }

    @Test
    @DisplayName("Должен обновить пост и вернуть обновленный объект с тегами")
    void shouldUpdatePost() {
        PostRequest updateRequest = new PostRequest(POST_ID, "Updated Title", "Updated Text", List.of());
        PostResponse updatedPost = new PostResponse(POST_ID, updateRequest.title(), updateRequest.text(), List.of("tag1"), 5, 10);

        when(jdbcTemplate.queryForObject(
                eq(UPDATE_POST),
                any(RowMapper.class),
                eq(updateRequest.title()),
                eq(updateRequest.text()),
                any(Timestamp.class),
                eq(updateRequest.id())))
                .thenReturn(updatedPost);

        PostResponse result = postRepository.updatePost(updateRequest);

        assertEquals(updatedPost, result);
        assertTrue(result.tags().contains("tag1"));

        verify(jdbcTemplate).queryForObject(eq(UPDATE_POST), any(RowMapper.class), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Должен удалить пост успешно")
    void shouldDeletePostSuccessfully() {
        when(jdbcTemplate.update(DELETE_POST, POST_ID)).thenReturn(1);

        assertDoesNotThrow(() -> postRepository.deletePost(POST_ID));

        verify(jdbcTemplate).update(DELETE_POST, POST_ID);
    }

    @Test
    @DisplayName("Должен выбросить IllegalStateException при удалении несуществующего поста")
    void shouldThrowWhenDeleteNonExistingPost() {
        when(jdbcTemplate.update(DELETE_POST, NON_EXISTING_POST_ID)).thenReturn(0);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> postRepository.deletePost(NON_EXISTING_POST_ID));

        assertTrue(ex.getMessage().contains("Post to delete not found"));

        verify(jdbcTemplate).update(DELETE_POST, NON_EXISTING_POST_ID);
    }

    @Test
    @DisplayName("Должен вернуть список тегов для поста")
    void shouldReturnTagsForPost() {
        when(jdbcTemplate.query(eq(GET_TAGS_FOR_POST), (RowMapper<String>) any(), eq(POST_ID)))
                .thenReturn(List.of("tagX", "tagY"));

        List<String> tags = postRepository.getTagsForPost(POST_ID);

        assertEquals(2, tags.size());
        assertTrue(tags.containsAll(List.of("tagX", "tagY")));

        verify(jdbcTemplate).query(eq(GET_TAGS_FOR_POST), any(RowMapper.class), eq(POST_ID));
    }

    @Test
    @DisplayName("Должен вернуть пустой список тегов при исключении")
    void shouldReturnEmptyTagsListOnException() {
        when(jdbcTemplate.query(eq(GET_TAGS_FOR_POST), (RowMapper<String>) any(), eq(POST_ID)))
                .thenThrow(RuntimeException.class);

        List<String> tags = postRepository.getTagsForPost(POST_ID);

        assertNotNull(tags);
        assertTrue(tags.isEmpty());

        verify(jdbcTemplate).query(eq(GET_TAGS_FOR_POST), any(RowMapper.class), eq(POST_ID));
    }

    @Test
    @DisplayName("Должен успешно увеличить счетчик лайков")
    void shouldIncrementLikes() {
        when(jdbcTemplate.update(INCREMENT_LIKES, POST_ID)).thenReturn(1);

        assertDoesNotThrow(() -> postRepository.incrementLikes(POST_ID));

        verify(jdbcTemplate).update(INCREMENT_LIKES, POST_ID);
    }

    @Test
    @DisplayName("Должен выбросить EmptyResultDataAccessException если пост не найден при увеличении лайков")
    void shouldThrowWhenIncrementLikesNoPost() {
        when(jdbcTemplate.update(INCREMENT_LIKES, POST_ID)).thenReturn(0);

        EmptyResultDataAccessException ex = assertThrows(EmptyResultDataAccessException.class,
                () -> postRepository.incrementLikes(POST_ID));
        assertTrue(ex.getMessage().contains("not found"));

        verify(jdbcTemplate).update(INCREMENT_LIKES, POST_ID);
    }

    @Test
    @DisplayName("Должен увеличить счетчик комментариев")
    void shouldIncrementCommentsCount() {
        when(jdbcTemplate.update(INCREMENT_COMMENTS_COUNT, POST_ID)).thenReturn(1);

        assertDoesNotThrow(() -> postRepository.incrementCommentsCount(POST_ID));

        verify(jdbcTemplate).update(INCREMENT_COMMENTS_COUNT, POST_ID);
    }

    @Test
    @DisplayName("Должен уменьшить счетчик комментариев")
    void shouldDecrementCommentsCount() {
        when(jdbcTemplate.update(DECREMENT_COMMENTS_COUNT, POST_ID)).thenReturn(1);

        assertDoesNotThrow(() -> postRepository.decrementCommentsCount(POST_ID));

        verify(jdbcTemplate).update(DECREMENT_COMMENTS_COUNT, POST_ID);
    }
}

