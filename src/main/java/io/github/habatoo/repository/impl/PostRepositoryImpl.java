package io.github.habatoo.repository.impl;

import io.github.habatoo.dto.request.PostCreateRequest;
import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.repository.PostRepository;
import io.github.habatoo.repository.mapper.PostListRowMapper;
import io.github.habatoo.repository.sql.PostSqlQueries;
import io.github.habatoo.service.dto.PostCounters;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Реализация репозитория для работы с постами блога.
 * Обеспечивает доступ к данным постов с использованием JDBC Template
 * (только CRUD операции).
 *
 * @see PostListRowMapper
 * @see JdbcTemplate
 */
/**
 * Реализация репозитория для работы с постами.
 * Обеспечивает доступ к данным постов с использованием JDBC Template.
 */
@Repository
public class PostRepositoryImpl implements PostRepository {

    private final JdbcTemplate jdbcTemplate;
    private final PostListRowMapper postListRowMapper;

    /**
     * Конструктор репозитория.
     *
     * @param jdbcTemplate      JdbcTemplate для выполнения запросов
     * @param postListRowMapper маппер для постов
     */
    public PostRepositoryImpl(JdbcTemplate jdbcTemplate,
                              PostListRowMapper postListRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.postListRowMapper = postListRowMapper;
    }

    /**
     * Вставляет пост в базу данных и возвращает сгенерированный ID.
     *
     * @param postCreateRequest запрос на создание поста
     * @return сгенерированный идентификатор поста
     */
    @Override
    public Long insertPost(PostCreateRequest postCreateRequest) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    PostSqlQueries.INSERT_POST,
                    new String[]{"id"}
            );
            ps.setString(1, postCreateRequest.title());
            ps.setString(2, postCreateRequest.text());
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
     * Обновляет основные данные поста.
     *
     * @param postRequest запрос на обновление поста
     */
    @Override
    public void updatePostData(PostRequest postRequest) {
        int updatedRows = jdbcTemplate.update(
                PostSqlQueries.UPDATE_POST,
                postRequest.title(),
                postRequest.text(),
                Timestamp.valueOf(LocalDateTime.now()),
                postRequest.id()
        );

        if (updatedRows == 0) {
            throw new DataAccessException("Post was concurrently modified or deleted") {};
        }
    }

    /**
     * Получает счетчики лайков и комментариев для поста.
     *
     * @param postId идентификатор поста
     * @return объект с счетчиками
     */
    @Override
    public PostCounters getPostCounters(Long postId) {
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
     * Получает теги для конкретного поста.
     *
     * @param postId идентификатор поста
     * @return список имен тегов
     */
    @Override
    public List<String> getTagsForPost(Long postId) {
        try {
            return jdbcTemplate.query(
                    PostSqlQueries.GET_POST_TAG,
                    (rs, rowNum) -> rs.getString("name"),
                    postId
            );
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Получает имя файла изображения для поста.
     *
     * @param postId идентификатор поста
     * @return имя файла или null если изображение не установлено
     */
    @Override
    public String getImageFileName(Long postId) {
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

    /**
     * Проверяет существование поста.
     *
     * @param postId идентификатор поста
     * @return true если пост существует, false в противном случае
     */
    @Override
    public boolean postExists(Long postId) {
        Integer count = jdbcTemplate.queryForObject(
                PostSqlQueries.CHECK_POST_EXISTS,
                Integer.class,
                postId
        );
        return count != null && count > 0;
    }

    /**
     * Подсчитывает количество постов по поисковому запросу.
     *
     * @param search строка поиска для обработки
     * @return количество постов, соответствующих критериям поиска
     */
    @Override
    public Integer countPostsBySearch(String search) {
        List<String> searchWords = Arrays.stream(search.split("\\s+"))
                .filter(word -> !word.trim().isEmpty())
                .collect(Collectors.toList());

        List<String> tags = searchWords.stream()
                .filter(word -> word.startsWith("#"))
                .map(tag -> tag.substring(1))
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toList());

        List<String> textWords = searchWords.stream()
                .filter(word -> !word.startsWith("#"))
                .collect(Collectors.toList());

        String textSearchPattern = textWords.isEmpty() ? "" :
                "%" + String.join(" ", textWords) + "%";

        List<Object> params = new ArrayList<>();

        if (!textWords.isEmpty()) {
            params.add(textSearchPattern);
            params.add(textSearchPattern);
        }

        for (String tag : tags) {
            params.add("%" + tag + "%");
        }

        String sql = buildCountQuery(!textWords.isEmpty(), tags.size());

        return jdbcTemplate.queryForObject(sql, Integer.class, params.toArray());
    }

    /**
     * Находит посты по поисковому запросу с пагинацией (БЕЗ тегов)
     *
     * @param search строка поиска для обработки
     * @param pageSize размер страницы
     * @param offset смещение
     * @return список постов без тегов
     */
    @Override
    public List<PostResponse> findPostsBySearchPaginated(String search, int pageSize, int offset) {
        List<String> searchWords = Arrays.stream(search.split("\\s+"))
                .filter(word -> !word.trim().isEmpty())
                .collect(Collectors.toList());

        List<String> tags = searchWords.stream()
                .filter(word -> word.startsWith("#"))
                .map(tag -> tag.substring(1))
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toList());

        List<String> textWords = searchWords.stream()
                .filter(word -> !word.startsWith("#"))
                .collect(Collectors.toList());

        String textSearchPattern = textWords.isEmpty() ? "" :
                "%" + String.join(" ", textWords) + "%";

        List<Object> params = new ArrayList<>();

        if (!textWords.isEmpty()) {
            params.add(textSearchPattern);
            params.add(textSearchPattern);
        }

        for (String tag : tags) {
            params.add("%" + tag + "%");
        }

        params.add(pageSize);
        params.add(offset);

        String sql = buildSearchQuery(!textWords.isEmpty(), tags.size());

        return jdbcTemplate.query(sql, postListRowMapper, params.toArray());
    }

    /**
     * Находит пост по идентификатору.
     *
     * @param id идентификатор поста
     * @return Optional с постом или empty если не найден
     */
    @Override
    public Optional<PostResponse> findById(Long id) {
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

    /**
     * Получает количество лайков поста.
     *
     * @param postId идентификатор поста
     * @return количество лайков
     */
    @Override
    public Integer getLikesCount(Long postId) {
        return jdbcTemplate.queryForObject(
                PostSqlQueries.GET_LIKES_COUNT, Integer.class, postId
        );
    }

    /**
     * Увеличивает счетчик лайков.
     *
     * @param postId идентификатор поста
     */
    @Override
    public void incrementLikes(Long postId) {
        int updatedRows = jdbcTemplate.update(PostSqlQueries.INCREMENT_LIKES, postId);
        if (updatedRows == 0) {
            throw new EmptyResultDataAccessException("Post with id " + postId + " not found", 1);
        }
    }

    /**
     * Увеличивает счетчик комментариев.
     *
     * @param postId идентификатор поста
     */
    @Override
    public void incrementCommentsCount(Long postId) {
        int updatedRows = jdbcTemplate.update(PostSqlQueries.INCREMENT_COMMENTS_COUNT, postId);
        if (updatedRows == 0) {
            throw new EmptyResultDataAccessException("Post with id " + postId + " not found", 1);
        }
    }

    /**
     * Уменьшает счетчик комментариев.
     *
     * @param postId идентификатор поста
     */
    @Override
    public void decrementCommentsCount(Long postId) {
        int updatedRows = jdbcTemplate.update(PostSqlQueries.DECREMENT_COMMENTS_COUNT, postId);
        if (updatedRows == 0) {
            throw new EmptyResultDataAccessException("Post with id " + postId + " not found", 1);
        }
    }

    /**
     * Удаляет пост по идентификатору.
     *
     * @param id идентификатор поста
     * @return количество удаленных строк
     */
    @Override
    public int deleteById(Long id) {
        return jdbcTemplate.update(PostSqlQueries.DELETE_POST, id);
    }

    /**
     * Строит SQL запрос для поиска постов на основе условий.
     *
     * @param hasTextWords есть ли обычные слова для поиска
     * @param tagCount количество тегов для поиска
     * @return SQL запрос с соответствующими условиями
     */
    private String buildSearchQuery(boolean hasTextWords, int tagCount) {
        StringBuilder sql = new StringBuilder("""
            SELECT p.id, p.title, p.text, p.likes_count, p.comments_count 
            FROM post p
            WHERE 1=1
            """);

        if (hasTextWords) {
            sql.append(" AND (p.title ILIKE ? OR p.text ILIKE ?)");
        }

        if (tagCount > 0) {
            sql.append(" AND EXISTS (");
            sql.append("   SELECT 1 FROM post_tag pt ");
            sql.append("   JOIN tag t ON pt.tag_id = t.id ");
            sql.append("   WHERE pt.post_id = p.id AND (");

            for (int i = 0; i < tagCount; i++) {
                if (i > 0) {
                    sql.append(" AND ");
                }
                sql.append("t.name ILIKE ?");
            }

            sql.append(")");
            sql.append(")");
        }

        sql.append(" ORDER BY p.created_at DESC LIMIT ? OFFSET ?");

        return sql.toString();
    }

    /**
     * Строит SQL запрос для подсчета количества постов.
     *
     * @param hasTextWords есть ли обычные слова для поиска
     * @param tagCount количество тегов для поиска
     * @return SQL запрос для COUNT
     */
    private String buildCountQuery(boolean hasTextWords, int tagCount) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM post p WHERE 1=1");

        if (hasTextWords) {
            sql.append(" AND (p.title ILIKE ? OR p.text ILIKE ?)");
        }

        if (tagCount > 0) {
            sql.append(" AND EXISTS (");
            sql.append("   SELECT 1 FROM post_tag pt ");
            sql.append("   JOIN tag t ON pt.tag_id = t.id ");
            sql.append("   WHERE pt.post_id = p.id AND (");

            for (int i = 0; i < tagCount; i++) {
                if (i > 0) {
                    sql.append(" AND ");
                }
                sql.append("t.name ILIKE ?");
            }

            sql.append(")");
            sql.append(")");
        }

        return sql.toString();
    }
}