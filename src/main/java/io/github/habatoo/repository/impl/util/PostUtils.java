package io.github.habatoo.repository.impl.util;

import io.github.habatoo.dto.request.PostCreateRequest;
import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.model.PostTag;
import io.github.habatoo.model.Tag;
import io.github.habatoo.repository.PostTagRepository;
import io.github.habatoo.repository.TagRepository;
import io.github.habatoo.repository.sql.PostSqlQueries;
import io.github.habatoo.service.FileStorageService;
import lombok.experimental.UtilityClass;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Утилитный класс для операций с постами в базе данных.
 */
@UtilityClass
public final class PostUtils {

    public record PostCounters(Integer likesCount, Integer commentsCount) {
    }

    public record PaginationData(boolean hasPrev, boolean hasNext, int lastPage) {
    }

    /**
     * Вставляет пост в базу данных и возвращает сгенерированный ID.
     *
     * @param jdbcTemplate JdbcTemplate для выполнения запросов
     * @param postCreateRequest  запрос на создание поста
     * @return сгенерированный идентификатор поста
     * @throws IllegalStateException если не удалось получить сгенерированный ключ
     */
    public static Long insertPost(JdbcTemplate jdbcTemplate, PostCreateRequest postCreateRequest) {
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
     * Обрабатывает теги поста: создает или находит существующие теги и создает связи.
     *
     * @param tagRepository     репозиторий тегов
     * @param postTagRepository репозиторий связей пост-тег
     * @param postId            идентификатор поста
     * @param tagNames          список имен тегов
     * @return список имен тегов для ответа
     */
    public static List<String> processTags(
            TagRepository tagRepository,
            PostTagRepository postTagRepository,
            Long postId,
            List<String> tagNames) {

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
     * Проверяет существование поста.
     *
     * @param jdbcTemplate JdbcTemplate для выполнения запросов
     * @param postId       идентификатор поста
     * @return true если пост существует, false в противном случае
     */
    public static boolean postExists(JdbcTemplate jdbcTemplate, Long postId) {
        Integer count = jdbcTemplate.queryForObject(
                PostSqlQueries.CHECK_POST_EXISTS,
                Integer.class,
                postId
        );
        return count != null && count > 0;
    }

    /**
     * Обновляет основные данные поста.
     *
     * @param jdbcTemplate JdbcTemplate для выполнения запросов
     * @param postRequest  запрос на обновление поста
     * @throws DataAccessException если обновление не прошло
     */
    public static void updatePostData(JdbcTemplate jdbcTemplate, PostRequest postRequest) {
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

    /**
     * Получает счетчики лайков и комментариев для поста.
     *
     * @param jdbcTemplate JdbcTemplate для выполнения запросов
     * @param postId       идентификатор поста
     * @return объект с счетчиками
     */
    public static PostCounters getPostCounters(JdbcTemplate jdbcTemplate, Long postId) {
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
     * @param jdbcTemplate JdbcTemplate для выполнения запросов
     * @param postId       идентификатор поста
     * @return список имен тегов
     */
    public static List<String> getTagsForPost(JdbcTemplate jdbcTemplate, Long postId) {
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
     * Получает имя файла изображения для поста.
     *
     * @param jdbcTemplate JdbcTemplate для выполнения запросов
     * @param postId       идентификатор поста
     * @return имя файла или null если изображение не установлено
     */
    public static String getImageFileName(JdbcTemplate jdbcTemplate, Long postId) {
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
     * Удаляет файлы поста.
     *
     * @param fileStorageService сервис для работы с файлами
     * @param postId             идентификатор поста
     * @param imageFileName      имя файла изображения
     * @throws DataRetrievalFailureException если не удалось удалить файлы
     */
    public static void deletePostFiles(FileStorageService fileStorageService, Long postId, String imageFileName) {
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

    /**
     * Обогащает пост тегами.
     *
     * @param jdbcTemplate JdbcTemplate для выполнения запросов
     * @param post         пост для обогащения
     * @return пост с тегами
     */
    public static PostResponse enrichPostWithTags(JdbcTemplate jdbcTemplate, PostResponse post) {
        List<String> tags = getTagsForPost(jdbcTemplate, post.id());
        return new PostResponse(
                post.id(),
                post.title(),
                post.text(),
                tags,
                post.likesCount(),
                post.commentsCount()
        );
    }

    /**
     * Обогащает список постов тегами.
     *
     * @param jdbcTemplate JdbcTemplate для выполнения запросов
     * @param posts        список постов для обогащения
     * @return список постов с тегами
     */
    public static List<PostResponse> enrichPostsWithTags(JdbcTemplate jdbcTemplate, List<PostResponse> posts) {
        return posts.stream()
                .map(post -> enrichPostWithTags(jdbcTemplate, post))
                .toList();
    }

    /**
     * Вычисляет данные пагинации.
     *
     * @param totalCount общее количество элементов
     * @param pageNumber номер текущей страницы
     * @param pageSize   размер страницы
     * @return объект с данными пагинации
     */
    public static PaginationData calculatePagination(int totalCount, int pageNumber, int pageSize) {
        int lastPage = (int) Math.ceil((double) totalCount / pageSize);
        boolean hasPrev = pageNumber > 1;
        boolean hasNext = pageNumber < lastPage;
        return new PaginationData(hasPrev, hasNext, lastPage);
    }
}
