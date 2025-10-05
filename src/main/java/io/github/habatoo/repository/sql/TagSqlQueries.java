package io.github.habatoo.repository.sql;

import lombok.experimental.UtilityClass;

/**
 * Класс с SQL запросами для работы с тэгами.
 */
@UtilityClass
public class TagSqlQueries {

    public static final String SELECT_FROM_TAG = "SELECT id, name, created_at FROM tag WHERE name = ?";

    public static final String INSERT_INTO_TAG = "INSERT INTO tag (name, created_at) VALUES (?, ?)";

}
