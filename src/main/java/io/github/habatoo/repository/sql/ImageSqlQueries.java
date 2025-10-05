package io.github.habatoo.repository.sql;

import lombok.experimental.UtilityClass;

/**
 * Класс с SQL запросами для работы с изображениями.
 */
@UtilityClass
public final class ImageSqlQueries {

    public static final String CHECK_POST_EXISTS = "SELECT COUNT(*) FROM post WHERE id = ?";
    public static final String GET_IMAGE_FILE_NAME = "SELECT image_url FROM post WHERE id = ?";
    public static final String UPDATE_POST_IMAGE = """
        UPDATE post SET image_name = ?, image_size = ?, image_url = ?, updated_at = ? 
        WHERE id = ?
        """;
}