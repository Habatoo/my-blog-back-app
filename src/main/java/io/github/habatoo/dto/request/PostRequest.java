package io.github.habatoo.dto.request;

import java.util.List;

/**
 * DTO для запроса создания или обновления поста.
 * <p>
 * Содержит данные для создания нового поста или обновления существующего.
 * Используется как входящие данные для API endpoints создания и редактирования постов.
 * </p>
 *
 * @param id    уникальный идентификатор поста (обязателен для обновления, не используется при создании)
 * @param title название поста (обязательное поле)
 * @param text  текст поста в формате Markdown (обязательное поле)
 * @param tags  список тегов поста (не может быть null)
 */
public record PostRequest(
        Long id,
        String title,
        String text,
        List<String> tags
) {
}
