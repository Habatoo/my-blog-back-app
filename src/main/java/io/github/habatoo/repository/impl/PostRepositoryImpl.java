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

import static io.github.habatoo.repository.sql.PostSqlQueries.*;

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
                postListRowMapper,
                postCreateRequest.title(),
                postCreateRequest.text(),
                Timestamp.valueOf(now),
                Timestamp.valueOf(now)
        );

        long postId = post.id();
        List<String> tags = postCreateRequest.tags();
        if (!tags.isEmpty()) {
            jdbcTemplate.batchUpdate(
                    INSERT_INTO_TAG,
                    tags,
                    tags.size(),
                    (ps, tag) -> ps.setString(1, tag)
            );

            jdbcTemplate.batchUpdate(
                    INSERT_INTO_POST_TAG,
                    tags,
                    tags.size(),
                    (ps, tag) -> {
                        ps.setLong(1, postId);
                        ps.setString(2, tag);
                    }
            );
        }

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
                postListRowMapper,
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
