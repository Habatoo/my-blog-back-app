package io.github.habatoo.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO для создания нового поста.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequest {
    /**
     * Уникальный идентификатор поста.
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
    @Builder.Default
    private List<String> tags = new ArrayList<>();
}
