package io.github.habatoo.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для ответа с созданным постом
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
    /**
     * Сгенерированный идентификатор поста
     */
    private Long id;

    /**
     * Название поста
     */
    private String title;

    /**
     * Текст поста в формате Markdown
     */
    private String text;

    /**
     * Список тегов поста
     */
    private List<String> tags;

    /**
     * Количество лайков поста (всегда 0 при создании)
     */
    private Integer likesCount;

    /**
     * Количество комментариев поста (всегда 0 при создании)
     */
    private Integer commentsCount;
}
