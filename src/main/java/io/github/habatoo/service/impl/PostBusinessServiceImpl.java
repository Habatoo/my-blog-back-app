package io.github.habatoo.service.impl;

import io.github.habatoo.dto.request.PostCreateRequest;
import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.repository.PostRepository;
import io.github.habatoo.service.FileStorageService;
import io.github.habatoo.service.PostBusinessService;
import io.github.habatoo.service.PostValidationService;
import io.github.habatoo.service.TagService;
import io.github.habatoo.service.dto.PostCounters;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Сервис для бизнес-операций с постами
 */
@Service
public class PostBusinessServiceImpl implements PostBusinessService {

    private final PostRepository postRepository;
    private final TagService tagService;
    private final FileStorageService fileStorageService;
    private final PostValidationService validationService;

    public PostBusinessServiceImpl(PostRepository postRepository,
                                   TagService tagService,
                                   FileStorageService fileStorageService,
                                   PostValidationService validationService) {
        this.postRepository = postRepository;
        this.tagService = tagService;
        this.fileStorageService = fileStorageService;
        this.validationService = validationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostResponse createPostWithTags(PostCreateRequest postCreateRequest) {
        validationService.validatePostRequest(postCreateRequest);

        Long postId = postRepository.insertPost(postCreateRequest);
        List<String> processedTags = tagService.processPostTags(postId, postCreateRequest.tags());

        return PostResponse.forNewPost(postId, postCreateRequest.title(),
                postCreateRequest.text(), processedTags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostResponse updatePostWithTags(PostRequest postRequest) {
        validationService.validatePostRequest(postRequest);
        validationService.validatePostExists(postRequest.id());

        postRepository.updatePostData(postRequest);
        List<String> processedTags = tagService.processPostTags(postRequest.id(), postRequest.tags());
        PostCounters counters = postRepository.getPostCounters(postRequest.id());

        return new PostResponse(
                postRequest.id(),
                postRequest.title(),
                postRequest.text(),
                processedTags,
                counters.likesCount(),
                counters.commentsCount()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePostWithCleanup(Long postId) {
        validationService.validatePostId(postId);

        String imageFileName = postRepository.getImageFileName(postId);
        int affectedRows = postRepository.deleteById(postId);

        if (affectedRows == 0) {
            throw new EmptyResultDataAccessException("Post with id " + postId + " not found", 1);
        }

        deletePostFilesSafely(postId, imageFileName);
    }

    /**
     * Безопасно удаляет файлы поста, логируя ошибки вместо проброса исключений.
     *
     * @param postId        идентификатор поста
     * @param imageFileName имя файла изображения
     */
    private void deletePostFilesSafely(Long postId, String imageFileName) {
        if (imageFileName != null) {
            try {
                fileStorageService.deleteImageFile(imageFileName);
                fileStorageService.deletePostDirectory(postId);
            } catch (IOException e) {
                // Логируем ошибку, но не прерываем транзакцию TODO
                System.err.println("Failed to delete image file for post " + postId + ": " + e.getMessage());
                throw new DataRetrievalFailureException(
                        "Failed to delete image file for post " + postId, e
                );
            }
        }
    }
}
