package io.github.habatoo.repository.impl;

import io.github.habatoo.dto.request.PostCreateRequest;
import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.repository.PostRepository;
import io.github.habatoo.repository.mapper.PostListRowMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Реализация репозитория для работы с постами блога.
 * Обеспечивает доступ к данным постов с использованием JDBC Template
 * (только CRUD операции).
 *
 * @see PostListRowMapper
 * @see JdbcTemplate
 */
@Repository
public class PostRepositoryImpl implements PostRepository {

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

    private final JdbcTemplate jdbcTemplate;
    private final PostListRowMapper postListRowMapper;

    public PostRepositoryImpl(JdbcTemplate jdbcTemplate,
                              PostListRowMapper postListRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.postListRowMapper = postListRowMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PostResponse> findAllPosts() {
        List<PostResponse> posts = jdbcTemplate.query(FIND_ALL_POSTS, postListRowMapper);
        return posts.stream()
                .map(this::enrichWithTags)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostResponse createPost(PostCreateRequest postCreateRequest) {
        LocalDateTime now = LocalDateTime.now();
        PostResponse post = jdbcTemplate.queryForObject(
                CREATE_POST,
                (rs, rowNum) -> new PostResponse(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        List.of(), // пока пусто, заполним позже
                        rs.getInt("likes_count"),
                        rs.getInt("comments_count")
                ),
                postCreateRequest.title(),
                postCreateRequest.text(),
                Timestamp.valueOf(now),
                Timestamp.valueOf(now)
        );

        long postId = post.id();
        for (String tag : postCreateRequest.tags()) {
            jdbcTemplate.update("INSERT INTO tag (name) VALUES (?) ON CONFLICT (name) DO NOTHING", tag);
            Long tagId = jdbcTemplate.queryForObject("SELECT id FROM tag WHERE name = ?", Long.class, tag); // TODO
            jdbcTemplate.update("INSERT INTO post_tag (post_id, tag_id) VALUES (?, ?) ON CONFLICT DO NOTHING", postId, tagId);
        }

        List<String> tags = getTagsForPost(postId);
        return new PostResponse(
                postId, post.title(), post.text(), tags,
                post.likesCount(), post.commentsCount()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostResponse updatePost(PostRequest postRequest) {
        return jdbcTemplate.queryForObject(
                UPDATE_POST,
                (rs, rowNum) -> new PostResponse(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        getTagsForPost(rs.getLong("id")),
                        rs.getInt("likes_count"),
                        rs.getInt("comments_count")
                ),
                postRequest.title(),
                postRequest.text(),
                Timestamp.valueOf(LocalDateTime.now()),
                postRequest.id()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePost(Long id) {
        int deletedRows = jdbcTemplate.update(DELETE_POST, id);
        if (deletedRows == 0) {
            throw new IllegalStateException("Post to delete not found with id " + id);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getTagsForPost(Long postId) {
        try {
            return jdbcTemplate.query(GET_TAGS_FOR_POST,
                    (rs, rowNum) -> rs.getString("name"),
                    postId
            );
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incrementLikes(Long postId) {
        int updatedRows = jdbcTemplate.update(INCREMENT_LIKES, postId);
        if (updatedRows == 0) {
            throw new EmptyResultDataAccessException("Post with id " + postId + " not found", 1);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incrementCommentsCount(Long postId) {
        jdbcTemplate.update(INCREMENT_COMMENTS_COUNT, postId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void decrementCommentsCount(Long postId) {
        jdbcTemplate.update(DECREMENT_COMMENTS_COUNT, postId);
    }

    private PostResponse enrichWithTags(PostResponse post) {
        List<String> tags = getTagsForPost(post.id());
        return new PostResponse(
                post.id(),
                post.title(),
                post.text(),
                tags,
                post.likesCount(),
                post.commentsCount()
        );
    }

}
