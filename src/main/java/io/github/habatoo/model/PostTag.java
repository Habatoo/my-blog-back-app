package io.github.habatoo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Сущность, представляющая связь между постом и тегом.
 *
 * <p>Используется для реализации отношения многие-ко-многим между
 * постами и тегами через таблицу связи {@code post_tag}.</p>
 *
 * <p>Каждый экземпляр этого класса соответствует одной записи
 * в таблице связи и содержит идентификаторы связанных сущностей
 * и метку времени создания связи.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostTag {

    /**
     * Идентификатор поста, связанного с тегом.
     */
    private Long postId;

    /**
     * Идентификатор тега, связанного с постом.
     */
    private Long tagId;

    /**
     * Дата и время создания связи между постом и тегом.
     */
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
