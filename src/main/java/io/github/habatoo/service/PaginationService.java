package io.github.habatoo.service;


import io.github.habatoo.dto.response.PostListResponse;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.service.dto.PaginationData;

import java.util.List;

/**
 * Контракт пагинации
 */
public interface PaginationService {

    /**
     * Вычисляет данные пагинации.
     *
     * @param totalCount общее количество элементов
     * @param pageNumber номер текущей страницы
     * @param pageSize   размер страницы
     * @return объект с данными пагинации
     */
    PaginationData calculatePagination(int totalCount, int pageNumber, int pageSize);

    /**
     * Создает ответ с пагинацией для списка постов.
     *
     * @param posts      список постов
     * @param totalCount общее количество постов
     * @param pageNumber номер страницы
     * @param pageSize   размер страницы
     * @return ответ с пагинацией
     */
     PostListResponse createPostListResponse(
             List<PostResponse> posts,
             int totalCount,
             int pageNumber,
             int pageSize);
}
