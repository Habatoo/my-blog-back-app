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

    private final JdbcTemplate jdbcTemplate;
    private final PostListRowMapper postListRowMapper;

    public PostRepositoryImpl(JdbcTemplate jdbcTemplate,
                              PostListRowMapper postListRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.postListRowMapper = postListRowMapper;
    }

    @Override
    public List<PostResponse> findAllPosts() {
        String sql = """
                SELECT p.id, p.title, p.text, p.likes_count, p.comments_count
                FROM post p
                ORDER BY p.created_at DESC
                """;
        List<PostResponse> posts = jdbcTemplate.query(sql, postListRowMapper);

        return posts.stream()
                .map(this::enrichWithTags)
                .toList();
    }

    @Override
    public PostResponse createPost(PostCreateRequest postCreateRequest) {
        String sql = """
                INSERT INTO post (title, text, likes_count, comments_count, created_at, updated_at)
                VALUES (?, ?, 0, 0, ?, ?)
                RETURNING id, title, text, likes_count, comments_count
                """;
        LocalDateTime now = LocalDateTime.now();

        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> new PostResponse(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        getTagsForPost(rs.getLong("id")),
                        rs.getInt("likes_count"),
                        rs.getInt("comments_count")
                ),
                postCreateRequest.title(),
                postCreateRequest.text(),
                Timestamp.valueOf(now),
                Timestamp.valueOf(now)
        );
    }

    @Override
    public PostResponse updatePost(PostRequest postRequest) {
        String sql = """
                UPDATE post
                SET title = ?, text = ?, updated_at = ?
                WHERE id = ?
                RETURNING id, title, text, likes_count, comments_count
                """;

        return jdbcTemplate.queryForObject(
                sql,
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

    @Override
    public void deletePost(Long id) {
        String sql = """
                DELETE FROM post WHERE id = ?
                """;
        int deletedRows = jdbcTemplate.update(sql, id);
        if (deletedRows == 0) {
            throw new IllegalStateException("Post to delete not found with id " + id);
        }
    }

    @Override
    public List<String> getTagsForPost(Long postId) {
        try {
            String sql = """
                    SELECT t.name FROM tag t
                    JOIN post_tag pt ON t.id = pt.tag_id
                    WHERE pt.post_id = ?
                    """;
            return jdbcTemplate.query(sql,
                    (rs, rowNum) -> rs.getString("name"),
                    postId
            );
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public void incrementLikes(Long postId) {
        String sql = """
                UPDATE post SET likes_count = likes_count + 1 WHERE id = ?
                """;
        int updatedRows = jdbcTemplate.update(sql, postId);
        if (updatedRows == 0) {
            throw new EmptyResultDataAccessException("Post with id " + postId + " not found", 1);
        }
    }

    @Override
    public void incrementCommentsCount(Long postId) {
        String sql = "UPDATE post SET comments_count = comments_count + 1 WHERE id = ?";
        jdbcTemplate.update(sql, postId);
    }

    @Override
    public void decrementCommentsCount(Long postId) {
        String sql = """
                UPDATE post
                SET comments_count = CASE WHEN comments_count > 0 THEN comments_count - 1 ELSE 0 END
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, postId);
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
