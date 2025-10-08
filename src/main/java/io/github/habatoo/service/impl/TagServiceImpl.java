package io.github.habatoo.service.impl;

import io.github.habatoo.model.PostTag;
import io.github.habatoo.model.Tag;
import io.github.habatoo.repository.PostTagRepository;
import io.github.habatoo.repository.TagRepository;
import io.github.habatoo.service.TagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Сервис для работы с тегами
 */
@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;

    /**
     * Конструктор сервиса тегов.
     *
     * @param tagRepository     репозиторий тегов
     * @param postTagRepository репозиторий связей пост-тег
     */
    public TagServiceImpl(TagRepository tagRepository, PostTagRepository postTagRepository) {
        this.tagRepository = tagRepository;
        this.postTagRepository = postTagRepository;
    }

    /**
     * Обрабатывает теги поста: создает или находит существующие теги и создает связи.
     *
     * @param postId   идентификатор поста
     * @param tagNames список имен тегов
     * @return список имен тегов для ответа
     */
    @Transactional
    public List<String> processPostTags(Long postId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return List.of();
        }

        List<String> validTagNames = tagNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .toList();

        if (validTagNames.isEmpty()) {
            return List.of();
        }

        List<String> processedTags = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (String tagName : validTagNames) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(tagName));

            postTagRepository.save(PostTag.builder()
                    .postId(postId)
                    .tagId(tag.getId())
                    .createdAt(now)
                    .build());

            processedTags.add(tagName);
        }

        return processedTags;
    }
}
