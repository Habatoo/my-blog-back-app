package io.github.habatoo.repository.impl;

import io.github.habatoo.dto.request.PostCreateRequest;
import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostListResponse;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.repository.PostRepository;
import io.github.habatoo.repository.PostTagRepository;
import io.github.habatoo.repository.TagRepository;
import io.github.habatoo.repository.impl.util.PostValidationUtils;
import io.github.habatoo.repository.mapper.RowMappers;
import io.github.habatoo.repository.sql.PostSqlQueries;
import io.github.habatoo.service.FileStorageService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static io.github.habatoo.repository.impl.util.PostUtils.*;

/**
 * Реализация репозитория для работы с постами блога.
 * Обеспечивает доступ к данным постов с использованием JDBC Template.
 *
 * <p>Данная реализация загружает посты вместе со связанными тегами и комментариями
 * в одном SQL запросе, используя JOIN операции для оптимизации производительности.</p>
 *
 * @see PostRepository
 * @see JdbcTemplate
 */
@Repository
public class PostRepositoryImpl implements PostRepository {

    private final JdbcTemplate jdbcTemplate;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final FileStorageService fileStorageService;
    private final RowMappers.PostListRowMapper postListRowMapper;

    public PostRepositoryImpl(
            JdbcTemplate jdbcTemplate,
            TagRepository tagRepository,
            PostTagRepository postTagRepository,
            FileStorageService fileStorageService,
            RowMappers.PostListRowMapper postListRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.tagRepository = tagRepository;
        this.postTagRepository = postTagRepository;
        this.fileStorageService = fileStorageService;
        this.postListRowMapper = postListRowMapper;
    }

    /**
     * {@inheritDoc}
     */
    //TODO - Строка поиска разбивается на слова по пробелам.
    //Пустые слова удаляются из поиска.
    //Слова, начинающиеся с #, считаются тегами, и посты фильтруются по ним по «И».
    //Слова, не начинающиеся с #, склеиваются вместе через пробел и считаются подстрокой поиска по названию.
    //Фильтрация постов по подстроке и тегам происходит по «И».
    @Override
    public PostListResponse findPostsWithPagination(String search, int pageNumber, int pageSize) {
        PostValidationUtils.validatePaginationParams(search, pageNumber, pageSize);

        int offset = (pageNumber - 1) * pageSize;
        String searchPattern = "%" + search + "%";

        try {
            Integer totalCount = jdbcTemplate.queryForObject(
                    PostSqlQueries.COUNT_POSTS_BY_SEARCH,
                    Integer.class,
                    searchPattern, searchPattern, searchPattern
            );

            if (totalCount == null || totalCount == 0) {
                return new PostListResponse(List.of(), false, false, 0);
            }

            List<PostResponse> posts = jdbcTemplate.query(
                    PostSqlQueries.FIND_POSTS_BY_SEARCH_PAGINATED,
                    postListRowMapper,
                    searchPattern, searchPattern, searchPattern,
                    pageSize, offset
            );

            List<PostResponse> postsWithTags = enrichPostsWithTags(jdbcTemplate, posts);
            PaginationData pagination = calculatePagination(totalCount, pageNumber, pageSize);

            return new PostListResponse(
                    postsWithTags,
                    pagination.hasPrev(),
                    pagination.hasNext(),
                    pagination.lastPage()
            );

        } catch (DataAccessException e) {
            throw new DataAccessException("Database error while searching posts", e) {
            };
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<PostResponse> findByIdWithFullContent(Long id) {
        PostValidationUtils.validatePostId(id);

        Optional<PostResponse> post = findPostById(id);
        return post.map(p -> enrichPostWithTags(jdbcTemplate, p));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public PostResponse save(PostCreateRequest postCreateRequest) {
        PostValidationUtils.validatePostRequest(postCreateRequest);

        Long postId = insertPost(jdbcTemplate, postCreateRequest);
        List<String> tagNames = processTags(tagRepository, postTagRepository, postId, postCreateRequest.tags());

        return PostResponse.forNewPost(postId, postCreateRequest.title(), postCreateRequest.text(), tagNames);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public PostResponse update(PostRequest postRequest) {
        PostValidationUtils.validatePostRequest(postRequest);
        PostValidationUtils.validatePostId(postRequest.id());

        if (!postExists(jdbcTemplate, postRequest.id())) {
            throw new EmptyResultDataAccessException("Post with id " + postRequest.id() + " not found", 1);
        }

        updatePostData(jdbcTemplate, postRequest);
        List<String> tagNames = processTags(tagRepository, postTagRepository, postRequest.id(), postRequest.tags());
        PostCounters counters = getPostCounters(jdbcTemplate, postRequest.id());

        return new PostResponse(
                postRequest.id(),
                postRequest.title(),
                postRequest.text(),
                tagNames,
                counters.likesCount(),
                counters.commentsCount()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteById(Long id) {
        PostValidationUtils.validatePostId(id);

        String imageFileName = getImageFileName(jdbcTemplate, id);
        int affectedRows = jdbcTemplate.update(PostSqlQueries.DELETE_POST, id);

        if (affectedRows == 0) {
            throw new EmptyResultDataAccessException("Post with id " + id + " not found", 1);
        }

        deletePostFiles(fileStorageService, id, imageFileName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public int incrementLikes(Long id) {
        PostValidationUtils.validatePostId(id);

        int updatedRows = jdbcTemplate.update(PostSqlQueries.INCREMENT_LIKES, id);
        if (updatedRows == 0) {
            throw new EmptyResultDataAccessException("Post with id " + id + " not found", 1);
        }

        Integer likesCount = jdbcTemplate.queryForObject(
                PostSqlQueries.GET_LIKES_COUNT, Integer.class, id
        );

        if (likesCount == null) {
            throw new IllegalStateException("Likes count is null for post with id " + id);
        }

        return likesCount;
    }

    private Optional<PostResponse> findPostById(Long id) {
        try {
            PostResponse post = jdbcTemplate.queryForObject(
                    PostSqlQueries.FIND_POST_BY_ID,
                    postListRowMapper,
                    id
            );
            return Optional.ofNullable(post);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
