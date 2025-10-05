package io.github.habatoo.repository.sql;

import lombok.experimental.UtilityClass;

/**
 * Класс с SQL запросами для работы со связанными постами/тэгами.
 */
@UtilityClass
public class PostTagSqlQueries {

    public static final String INSERT_INTO_POST_TAG =
            "INSERT INTO post_tag (post_id, tag_id, created_at) VALUES (?, ?, ?)";

}
