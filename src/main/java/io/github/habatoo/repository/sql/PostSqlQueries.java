package io.github.habatoo.repository.sql;

import lombok.experimental.UtilityClass;

/**
 * SQL запросы для работы с постами
 */
@UtilityClass
public final class PostSqlQueries {

    public static final String INSERT_POST = """
        INSERT INTO post (title, text, likes_count, comments_count, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?)
        """;

    public static final String UPDATE_POST = """
        UPDATE post SET title = ?, text = ?, updated_at = ? WHERE id = ?
        """;

    public static final String FIND_POST_COUNTERS = """
        SELECT likes_count, comments_count FROM post WHERE id = ?
        """;

    public static final String GET_POST_TAG = """
        SELECT t.name FROM tag t
        JOIN post_tag pt ON t.id = pt.tag_id
        WHERE pt.post_id = ?
        """;

    public static final String GET_IMAGE_FILE_NAME = """
        SELECT image_name FROM post WHERE id = ?
        """;

    public static final String CHECK_POST_EXISTS = """
        SELECT COUNT(*) FROM post WHERE id = ?
        """;

    public static final String COUNT_POST_BY_SEARCH = """
        SELECT COUNT(*) FROM post
        WHERE title ILIKE ? OR text ILIKE ? OR EXISTS (
            SELECT 1 FROM post_tag pt
            JOIN tag t ON pt.tag_id = t.id
            WHERE pt.post_id = post.id AND t.name ILIKE ?
        )
        """;

    public static final String FIND_POST_BY_SEARCH_PAGINATED = """
        SELECT p.id, p.title, p.text, p.likes_count, p.comments_count
        FROM post p
        WHERE p.title ILIKE ? OR p.text ILIKE ? OR EXISTS (
            SELECT 1 FROM post_tag pt
            JOIN tag t ON pt.tag_id = t.id
            WHERE pt.post_id = p.id AND t.name ILIKE ?
        )
        ORDER BY p.created_at DESC
        LIMIT ? OFFSET ?
        """;

    public static final String FIND_POST_BY_ID = """
        SELECT id, title, text, likes_count, comments_count
        FROM post WHERE id = ?
        """;

    public static final String GET_LIKES_COUNT = """
        SELECT likes_count FROM post WHERE id = ?
        """;

    public static final String INCREMENT_LIKES = """
        UPDATE post SET likes_count = likes_count + 1 WHERE id = ?
        """;

    public static final String INCREMENT_COMMENTS_COUNT = """
        UPDATE post SET comments_count = comments_count + 1 WHERE id = ?
        """;

    public static final String DECREMENT_COMMENTS_COUNT = """
        UPDATE post SET comments_count = comments_count - 1 WHERE id = ?
        """;

    public static final String DELETE_POST = """
        DELETE FROM post WHERE id = ?
        """;

}
