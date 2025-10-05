package io.github.habatoo.repository.mapper;

import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.util.TextUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Утилитный класс с RowMapper'ами для маппинга постов.
 */
public class PostRowMappers {

    /**
     * RowMapper для маппинга списка постов с пагинацией.
     * Обрезает текст до 128 символов для превью.
     */
    @Component
    public static class PostListRowMapper implements RowMapper<PostResponse> {

        @Override
        public PostResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            String title = rs.getString("title");
            String text = TextUtils.truncateText(rs.getString("text"), 128);
            Integer likesCount = rs.getInt("likes_count");
            Integer commentsCount = rs.getInt("comments_count");

            return new PostResponse(id, title, text, java.util.List.of(), likesCount, commentsCount);
        }
    }
}
