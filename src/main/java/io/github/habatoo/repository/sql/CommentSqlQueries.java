package io.github.habatoo.repository.sql;

import lombok.experimental.UtilityClass;

/**
 * SQL запросы для работы с комментариями.
 * Содержит все SQL выражения, используемые в CommentRepository.
 */
@UtilityClass
public final class CommentSqlQueries {

    // SELECT queries
    public static final String FIND_BY_POST_ID = """
            SELECT id, post_id, text
            FROM comment
            WHERE post_id = ?
            ORDER BY created_at DESC
            """;

    public static final String FIND_BY_POST_ID_AND_ID = """
            SELECT c.id, c.post_id, c.text
            FROM comment c
            INNER JOIN post p ON c.post_id = p.id
            WHERE p.id = ? AND c.id = ?
            """;

    public static final String COUNT_POST_EXISTS = "SELECT COUNT(*) FROM post WHERE id = ?";

    public static final String COUNT_COMMENT_EXISTS = "SELECT COUNT(*) FROM comment WHERE id = ? AND post_id = ?";

    // INSERT queries
    public static final String INSERT_COMMENT = """
            INSERT INTO comment (post_id, text, created_at, updated_at)
            VALUES (?, ?, ?, ?)
            """;

    // UPDATE queries
    public static final String UPDATE_COMMENT_TEXT = """
            UPDATE comment
            SET text = ?, updated_at = ?
            WHERE id = ? AND post_id = ?
            """;

    // DELETE queries
    public static final String DELETE_COMMENT = "DELETE FROM comment WHERE id = ?";
}
