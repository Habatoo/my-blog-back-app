package io.github.habatoo.repository.sql;

import lombok.experimental.UtilityClass;

/**
 * Класс с SQL запросами для работы с постами.
 */
@UtilityClass
public final class PostSqlQueries {

    // Запросы для пагинации
    public static final String COUNT_POSTS_BY_SEARCH = """
        SELECT COUNT(DISTINCT p.id)
        FROM post p
        LEFT JOIN post_tag pt ON p.id = pt.post_id
        LEFT JOIN tag t ON pt.tag_id = t.id
        WHERE p.title LIKE ? OR p.text LIKE ? OR t.name LIKE ?
        """;

    public static final String FIND_POSTS_BY_SEARCH_PAGINATED = """
        SELECT DISTINCT
            p.id, p.title, p.text, p.likes_count, p.comments_count, p.created_at
        FROM post p
        LEFT JOIN post_tag pt ON p.id = pt.post_id
        LEFT JOIN tag t ON pt.tag_id = t.id
        WHERE p.title LIKE ? OR p.text LIKE ? OR t.name LIKE ?
        ORDER BY p.created_at DESC
        LIMIT ? OFFSET ?
        """;

    // Запросы для отдельных операций
    public static final String FIND_POST_BY_ID = """
        SELECT id, title, text, likes_count, comments_count 
        FROM post WHERE id = ?
        """;

    public static final String FIND_POST_COUNTERS = """
        SELECT likes_count, comments_count FROM post WHERE id = ?
        """;

    public static final String INSERT_POST = """
        INSERT INTO post (title, text, likes_count, comments_count, created_at, updated_at) 
        VALUES (?, ?, ?, ?, ?, ?)
        """;

    public static final String UPDATE_POST = """
        UPDATE post SET title = ?, text = ?, updated_at = ? WHERE id = ?
        """;

    public static final String DELETE_POST = "DELETE FROM post WHERE id = ?";

    public static final String INCREMENT_LIKES = """
        UPDATE post SET likes_count = likes_count + 1 WHERE id = ?
        """;

    public static final String GET_LIKES_COUNT = "SELECT likes_count FROM post WHERE id = ?";

    public static final String GET_IMAGE_FILE_NAME = "SELECT image_url FROM post WHERE id = ?";

    public static final String CHECK_POST_EXISTS = "SELECT COUNT(*) FROM post WHERE id = ?";

    public static final String DELETE_POST_TAGS = "DELETE FROM post_tag WHERE post_id = ?";

    public static final String GET_POST_TAGS = """
        SELECT t.name FROM tag t 
        JOIN post_tag pt ON t.id = pt.tag_id 
        WHERE pt.post_id = ?
        """;
}
