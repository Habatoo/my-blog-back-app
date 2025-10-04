package io.github.habatoo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Сущность, представляющая комментарий к посту в блоге.
 *
 * <p>Содержит информацию о комментарии, включая текст, метаданные
 * и связь с родительским постом через отношение многие-к-одному.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    /**
     * Уникальный идентификатор комментария.
     */
    private Long id;

    /**
     * Идентификатор поста, к которому относится комментарий.
     */
    private Long postId;

    /**
     * Текст комментария.
     */
    private String text;

    /**
     * Дата и время создания комментария.
     */
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления комментария.
     */
    private LocalDateTime updatedAt;
}
