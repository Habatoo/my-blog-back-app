package io.github.habatoo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    /**
     * Уникальный идентификатор тэга.
     */
    private Long id;

    private String name;

    /**
     * Дата и время создания тэга.
     */
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

}
