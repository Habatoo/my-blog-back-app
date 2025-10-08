package io.github.habatoo.service;

import io.github.habatoo.dto.request.PostCreateRequest;
import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostResponse;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для бизнес-операций с постами
 */
public interface PostBusinessService {

    /**
     * Создает пост с обработкой тегов.
     *
     * @param postCreateRequest запрос на создание поста
     * @return созданный пост
     */
    @Transactional
    public PostResponse createPostWithTags(PostCreateRequest postCreateRequest);

    /**
     * Обновляет пост с обработкой тегов.
     *
     * @param postRequest запрос на обновление поста
     * @return обновленный пост
     */
    @Transactional
    public PostResponse updatePostWithTags(PostRequest postRequest);

    /**
     * Удаляет пост с очисткой связанных ресурсов.
     *
     * @param postId идентификатор поста
     */
    @Transactional
    public void deletePostWithCleanup(Long postId);
}
