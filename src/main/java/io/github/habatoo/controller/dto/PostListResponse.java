package io.github.habatoo.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO для ответа со списком постов
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostListResponse {
    private List<PostResponse> posts;
    private boolean hasPrev;
    private boolean hasNext;
    private int lastPage;
}
