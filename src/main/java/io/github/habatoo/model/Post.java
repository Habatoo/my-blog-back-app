package io.github.habatoo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Основная сущность, представляющая пост в блоге.
 *
 * <p>Содержит всю информацию о посте, включая контент, метаданные
 * и связанные сущности через отношения один-ко-многим и многие-ко-многим.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    /**
     * Уникальный идентификатор поста.
     */
    private Long id;

    /**
     * Заголовок поста.
     */
    private String title;

    /**
     * Основной текст поста в формате Markdown.
     */
    private String text;

    /**
     * Количество лайков поста
     */
    @Builder.Default
    private Integer likesCount = 0;

    /**
     * Количество комментариев поста
     */
    @Builder.Default
    private Integer commentsCount = 0;

    /**
     * URL или путь к изображению поста.
     */
    private String imageUrl;

    /**
     * Оригинальное имя файла изображения.
     */
    private String imageName;

    /**
     * Размер файла изображения в байтах.
     */
    private Integer imageSize;

    /**
     * Дата и время создания поста
     */
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Дата и время обновления поста
     */
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Набор тегов, связанных с постом через отношение многие-ко-многим.
     */
    @Builder.Default
    private Set<PostTag> tags = new HashSet<>();

    /**
     * Набор комментариев, связанных с постом через отношение один-ко-многим.
     */
    @Builder.Default
    private Set<Comment> comments = new HashSet<>();
}
