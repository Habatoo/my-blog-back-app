package io.github.habatoo.repository.impl.util;

import lombok.experimental.UtilityClass;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static io.github.habatoo.repository.sql.CommentSqlQueries.*;

/**
 * Утилитный класс для операций с комментариями в базе данных.
 */
@UtilityClass
public final class CommentUtils {

    /**
     * Вставляет комментарий в базу и возвращает сгенерированный ID.
     *
     * @param jdbcTemplate JdbcTemplate для выполнения запросов
     * @param postId       идентификатор поста
     * @param text         текст комментария
     * @return сгенерированный идентификатор комментария
     * @throws DataRetrievalFailureException если не удалось получить сгенерированный ключ
     */
    public static Long insertComment(JdbcTemplate jdbcTemplate, Long postId, String text) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_COMMENT, new String[]{"id"});
            ps.setLong(1, postId);
            ps.setString(2, text);
            LocalDateTime now = LocalDateTime.now();
            ps.setTimestamp(3, Timestamp.valueOf(now));
            ps.setTimestamp(4, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new DataRetrievalFailureException("Failed to retrieve generated key for comment");
        }

        return key.longValue();
    }

    /**
     * Обновляет счетчик комментариев поста.
     *
     * @param jdbcTemplate JdbcTemplate для выполнения запросов
     * @param postId       идентификатор поста
     * @param delta        изменение счетчика (положительное или отрицательное)
     * @throws DataAccessException если обновление не прошло
     */
    public static void updatePostCommentsCount(JdbcTemplate jdbcTemplate, Long postId, int delta) {
        int updatedRows = jdbcTemplate.update(
                UPDATE_POST_COMMENTS_COUNT,
                delta,
                Timestamp.valueOf(LocalDateTime.now()),
                postId
        );

        if (updatedRows == 0) {
            throw new DataAccessException("Failed to update post comments count for post id: " + postId) {
            };
        }
    }

    /**
     * Проверяет существование поста.
     *
     * @param jdbcTemplate JdbcTemplate для выполнения запросов
     * @param postId       идентификатор поста
     * @return true если пост существует, false в противном случае
     */
    public static boolean postExists(JdbcTemplate jdbcTemplate, Long postId) {
        Integer count = jdbcTemplate.queryForObject(COUNT_POST_EXISTS, Integer.class, postId);
        return count != null && count > 0;
    }

    /**
     * Проверяет существование комментария.
     *
     * @param jdbcTemplate JdbcTemplate для выполнения запросов
     * @param postId       идентификатор поста
     * @param commentId    идентификатор комментария
     * @return true если комментарий существует, false в противном случае
     */
    public static boolean commentExists(JdbcTemplate jdbcTemplate, Long postId, Long commentId) {
        Integer count = jdbcTemplate.queryForObject(COUNT_COMMENT_EXISTS, Integer.class, commentId, postId);
        return count != null && count > 0;
    }

    /**
     * Валидирует идентификаторы поста и комментария.
     *
     * @param postId    идентификатор поста
     * @param commentId идентификатор комментария
     * @throws IllegalArgumentException если идентификаторы невалидны
     */
    public static void validateIds(Long postId, Long commentId) {
        if (postId == null) {
            throw new IllegalArgumentException("Post ID cannot be null");
        }
        if (commentId == null) {
            throw new IllegalArgumentException("Comment ID cannot be null");
        }
    }

    /**
     * Валидирует текст комментария.
     *
     * @param text текст комментария
     * @throws IllegalArgumentException если текст невалиден
     */
    public static void validateCommentText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment text cannot be null or empty");
        }
    }
}