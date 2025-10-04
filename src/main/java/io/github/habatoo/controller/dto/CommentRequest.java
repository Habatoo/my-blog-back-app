package io.github.habatoo.controller.dto;

/**
 * DTO для запроса создания комментария
 */
public record CommentRequest(String text, Long postId) {
}
