package io.github.habatoo.repository.impl;

import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostListResponse;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.model.PostTag;
import io.github.habatoo.model.Tag;
import io.github.habatoo.repository.PostRepository;
import io.github.habatoo.repository.PostTagRepository;
import io.github.habatoo.repository.TagRepository;
import io.github.habatoo.repository.mapper.PostRowMappers;
import io.github.habatoo.repository.sql.PostSqlQueries;
import io.github.habatoo.service.FileStorageService;
import io.github.habatoo.util.ValidationUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    private final PostRowMappers.PostListRowMapper postListRowMapper;

    public PostRepositoryImpl(
            JdbcTemplate jdbcTemplate,
            TagRepository tagRepository,
            PostTagRepository postTagRepository,
            FileStorageService fileStorageService,
            PostRowMappers.PostListRowMapper postListRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.tagRepository = tagRepository;
        this.postTagRepository = postTagRepository;
        this.fileStorageService = fileStorageService;
        this.postListRowMapper = postListRowMapper;
    }

    /**
     * Поиск постов с пагинацией и фильтрами по запросу.
     *
     * @param search     строка поиска
     * @param pageNumber номер страницы
     * @param pageSize   размер страницы
     * @return объект фильтрованного списка постов с пагинацие
     */
    @Override
    public PostListResponse findPostsWithPagination(String search, int pageNumber, int pageSize) {
        ValidationUtils.validatePaginationParams(search, pageNumber, pageSize);

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

            List<PostResponse> postsWithTags = enrichPostsWithTags(posts);
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
     * Реализация поиска поста по ID с полной информацией.
     * Загружает пост с полным текстом, тегами и комментариями.
     *
     * @param id идентификатор поста
     * @return Optional с постом если найден
     */
    @Override
    public Optional<PostResponse> findByIdWithFullContent(Long id) {
        ValidationUtils.validatePostId(id);

        Optional<PostResponse> post = findPostById(id);
        return post.map(this::enrichPostWithTags);
    }

    /**
     * Сохраняет новый пост в базе данных на основе данных из запроса.
     * Выполняет вставку основных данных поста и обрабатывает связанные теги.
     *
     * @param postRequest объект запроса с данными для создания поста, содержащий
     *                    название, текст и список тегов
     * @return созданный объект {@link PostResponse} с присвоенным идентификатором и обработанными тегами
     * @throws IllegalStateException если исходный или сгенерированный ключ равен null
     * @throws DataAccessException   при ошибках доступа к базе данных
     */
    @Override
    @Transactional
    public PostResponse save(PostRequest postRequest) {
        ValidationUtils.validatePostRequest(postRequest);

        Long postId = insertPost(postRequest);
        List<String> tagNames = processTags(postId, postRequest.tags());

        return PostResponse.forNewPost(postId, postRequest.title(), postRequest.text(), tagNames);
    }

    /**
     * Обновляет существующий пост в базе данных на основе данных из запроса.
     * Модифицирует название, текст поста и временную метку обновления,
     * а также обрабатывает связанные теги.
     *
     * @param postRequest объект запроса с данными для обновления поста, содержащий
     *                    идентификатор, новое название, текст и список тегов
     * @return обновленный объект {@link PostResponse} с актуальными данными и тегами
     * @throws IllegalArgumentException       если запрос или ID null
     * @throws EmptyResultDataAccessException если пост с указанным ID не найден
     * @throws DataAccessException            при ошибках доступа к базе данных
     */
    @Override
    @Transactional
    public PostResponse update(PostRequest postRequest) {
        ValidationUtils.validatePostRequest(postRequest);
        ValidationUtils.validatePostId(postRequest.id());

        if (!postExists(postRequest.id())) {
            throw new EmptyResultDataAccessException("Post with id " + postRequest.id() + " not found", 1);
        }

        updatePostData(postRequest);
        List<String> tagNames = processTags(postRequest.id(), postRequest.tags());
        PostCounters counters = getPostCounters(postRequest.id());

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
     * Удаляет пост по идентификатору вместе со всеми связанными данными
     * включая файлы изображений на диске
     *
     * @param id идентификатор поста для удаления
     * @throws EmptyResultDataAccessException если пост с указанным идентификатором не найден
     * @throws DataRetrievalFailureException  если не удалось удалть файлы.папки изображений
     * @throws DataAccessException            при ошибках доступа к базе данных
     */
    @Override
    @Transactional
    public void deleteById(Long id) {
        ValidationUtils.validatePostId(id);

        String imageFileName = getImageFileName(id);
        int affectedRows = jdbcTemplate.update(PostSqlQueries.DELETE_POST, id);

        if (affectedRows == 0) {
            throw new EmptyResultDataAccessException("Post with id " + id + " not found", 1);
        }

        deletePostFiles(id, imageFileName);
    }

    /**
     * Увеличивает счетчик лайков поста на 1
     *
     * @param id идентификатор поста для увеличения лайков
     * @return обновленное количество лайков поста
     * @throws IllegalStateException          число лайков null
     * @throws EmptyResultDataAccessException если пост с указанным идентификатором не найден
     * @throws DataAccessException            при ошибках доступа к базе данных
     */
    @Override
    @Transactional
    public int incrementLikes(Long id) {
        ValidationUtils.validatePostId(id);

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

    private PostResponse enrichPostWithTags(PostResponse post) {
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

    private List<PostResponse> enrichPostsWithTags(List<PostResponse> posts) {
        return posts.stream()
                .map(this::enrichPostWithTags)
                .toList();
    }

    /**
     * Вставляет пост в базу данных и возвращает сгенерированный ID
     *
     * @param postRequest запрос на вставку
     * @return сгенерированный ID
     */
    private Long insertPost(PostRequest postRequest) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    PostSqlQueries.INSERT_POST,
                    new String[]{"id"}
            );
            ps.setString(1, postRequest.title());
            ps.setString(2, postRequest.text());
            ps.setInt(3, 0); // likesCount
            ps.setInt(4, 0); // commentsCount
            ps.setTimestamp(5, Timestamp.valueOf(now));
            ps.setTimestamp(6, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to generate primary key for new post");
        }

        return key.longValue();
    }

    /**
     * Обрабатывает теги поста: создает или находит существующие теги и создает связи
     *
     * @param postId   идентификатор поста
     * @param tagNames список имен тегов
     * @return список имен тегов для ответа
     */
    private List<String> processTags(Long postId, List<String> tagNames) {
        jdbcTemplate.update(PostSqlQueries.DELETE_POST_TAGS, postId);

        if (tagNames.isEmpty()) {
            return List.of();
        }

        List<String> validTagNames = tagNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .toList();

        if (validTagNames.isEmpty()) {
            return List.of();
        }

        List<String> processedTags = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (String tagName : validTagNames) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(tagName));

            postTagRepository.save(PostTag.builder()
                    .postId(postId)
                    .tagId(tag.getId())
                    .createdAt(now)
                    .build());

            processedTags.add(tagName);
        }

        return processedTags;
    }

    /**
     * Проверяет существование поста
     */
    private boolean postExists(Long postId) {
        Integer count = jdbcTemplate.queryForObject(
                PostSqlQueries.CHECK_POST_EXISTS,
                Integer.class,
                postId
        );
        return count != null && count > 0;
    }

    /**
     * Обновляет основные данные поста
     */
    private void updatePostData(PostRequest postRequest) {
        int updatedRows = jdbcTemplate.update(
                PostSqlQueries.UPDATE_POST,
                postRequest.title(),
                postRequest.text(),
                Timestamp.valueOf(LocalDateTime.now()),
                postRequest.id()
        );

        if (updatedRows == 0) {
            throw new DataAccessException("Post was concurrently modified or deleted") {
            };
        }
    }

    private PostCounters getPostCounters(Long postId) {
        try {
            return jdbcTemplate.queryForObject(
                    PostSqlQueries.FIND_POST_COUNTERS,
                    (rs, rowNum) -> new PostCounters(
                            rs.getInt("likes_count"),
                            rs.getInt("comments_count")
                    ),
                    postId
            );
        } catch (Exception e) {
            return new PostCounters(0, 0);
        }
    }

    /**
     * Получает теги для конкретного поста
     */
    private List<String> getTagsForPost(Long postId) {
        try {
            return jdbcTemplate.query(
                    PostSqlQueries.GET_POST_TAGS,
                    (rs, rowNum) -> rs.getString("name"),
                    postId
            );
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Получает имя файла изображения для поста
     *
     * @param postId идентификатор поста
     * @return имя файла или null если изображение не установлено
     */
    private String getImageFileName(Long postId) {
        try {
            return jdbcTemplate.queryForObject(
                    PostSqlQueries.GET_IMAGE_FILE_NAME,
                    String.class,
                    postId
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private void deletePostFiles(Long postId, String imageFileName) {
        if (imageFileName != null) {
            try {
                fileStorageService.deleteImageFile(imageFileName);
                fileStorageService.deletePostDirectory(postId);
            } catch (Exception e) {
                throw new DataRetrievalFailureException(
                        "Failed to delete image file for post " + postId, e
                );
            }
        }
    }

    private PaginationData calculatePagination(int totalCount, int pageNumber, int pageSize) {
        int lastPage = (int) Math.ceil((double) totalCount / pageSize);
        boolean hasPrev = pageNumber > 1;
        boolean hasNext = pageNumber < lastPage;
        return new PaginationData(hasPrev, hasNext, lastPage);
    }

    // Вспомогательные records
    private record PostCounters(Integer likesCount, Integer commentsCount) {
    }

    private record PaginationData(boolean hasPrev, boolean hasNext, int lastPage) {
    }

}
