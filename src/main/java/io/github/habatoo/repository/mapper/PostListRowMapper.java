package io.github.habatoo.repository.mapper;

import io.github.habatoo.dto.response.PostResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * RowMapper для маппинга списка постов с пагинацией.
 * Обрезает текст до 128 символов для превью.
 *
 * @see PostResponse
 */
@Component
public class PostListRowMapper implements RowMapper<PostResponse> {

    @Override
    public PostResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long id = rs.getLong("id");
        String title = rs.getString("title");
        String text = truncatePostText(rs.getString("text"));
        Integer likesCount = rs.getInt("likes_count");
        Integer commentsCount = rs.getInt("comments_count");

        return new PostResponse(id, title, text, java.util.List.of(), likesCount, commentsCount);
    }

    /**
     * Обрезает текст до указанной длины и добавляет "…" если нужно.
     *
     * @param text текст для обрезки
     * @return обрезанный текст
     */
    private String truncatePostText(String text) {
        if (text == null) return "";
        if (text.length() <= 128) return text;
        return text.substring(0, 128) + "…";
    }

}
