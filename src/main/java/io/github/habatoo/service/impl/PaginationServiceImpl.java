package io.github.habatoo.service.impl;

import io.github.habatoo.dto.response.PostListResponse;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.service.PaginationService;
import io.github.habatoo.service.dto.PaginationData;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Сервис пагинации
 */
@Service
public class PaginationServiceImpl implements PaginationService {

    /**
     * {@inheritDoc}
     */
    @Override
    public PaginationData calculatePagination(int totalCount, int pageNumber, int pageSize) {
        int lastPage = (int) Math.ceil((double) totalCount / pageSize);
        boolean hasPrev = pageNumber > 1;
        boolean hasNext = pageNumber < lastPage;
        return new PaginationData(hasPrev, hasNext, lastPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostListResponse createPostListResponse(List<PostResponse> posts, int totalCount,
                                                   int pageNumber, int pageSize) {
        PaginationData pagination = calculatePagination(totalCount, pageNumber, pageSize);
        return new PostListResponse(
                posts,
                pagination.hasPrev(),
                pagination.hasNext(),
                pagination.lastPage()
        );
    }
}
