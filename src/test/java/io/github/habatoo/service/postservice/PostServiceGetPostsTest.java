package io.github.habatoo.service.postservice;

import io.github.habatoo.dto.response.PostListResponse;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.service.impl.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Тесты метода getPosts класса PostServiceImpl
 */
@Disabled
@DisplayName("Тесты метода getPosts для проверки извлечения постов вместе со связанными сущностями.")
class PostServiceGetPostsTest extends PostServiceTestBase {

    @BeforeEach
    void setUp() {
        List<PostResponse> data = List.of(
                new PostResponse(1L, "Spring Framework", "Spring — это каркас...", List.of("java","backend"), 5, 2),
                new PostResponse(2L, "PostgreSQL Integration", "Настроим postgres...", List.of("db","backend"), 3, 1),
                new PostResponse(3L, "Советы по Markdown", "Учимся оформлять post", List.of("markdown"), 1, 0),
                new PostResponse(4L, "Framework Tips", "Framework rocks!", List.of("framework"), 2, 5),
                new PostResponse(5L, "Java Collections", "about HashMap и List", List.of("java"), 4, 3)
        );

        when(postRepository.findAllPosts()).thenReturn(data);
        postService = new PostServiceImpl(postRepository, fileStorageService);
    }

    @ParameterizedTest
    @CsvSource({
            "'', 1, 5, 5, false, false, 1", // Все посты, страница 1, hasNext/hasPrev
            "'Spring', 1, 5, 1, false, false, 1", // Только заголовок с подстрокой
            "'framework', 1, 5, 2, false, false, 1", // Подстрока встречается дважды (без учёта регистра)
            "'#java', 1, 5, 2, false, false, 1", // Только по тегу
            "'Spring #java', 1, 5, 1, false, false, 1", // Комбинация — строгая конъюнкция
            "'#backend', 1, 5, 2, false, false, 1", // посты с этим тегом
            "'#unknowntag', 1, 5, 0, false, false, 0", // тега нет — 0 постов
            "'hash', 1, 5, 1, false, false, 1", // только в тексте
            "'Java', 1, 2, 1, false, true, 3",  // Пагинация — Java встречается и как тег, и как часть слова
            "'Framework', 1, 10, 2, false, false, 1" // Проверка регистра в заголовке
    })
    void shouldFilterAndPaginatePosts(String search, int page, int size,
                                      int expectedCount, boolean hasPrev, boolean hasNext, int expectedLastPage) {
        PostListResponse result = postService.getPosts(search, page, size);
        assertEquals(expectedCount, result.posts().size());
        assertEquals(hasPrev, result.hasPrev());
        assertEquals(hasNext, result.hasNext());
        assertEquals(expectedLastPage, result.lastPage());

        for (PostResponse p : result.posts()) {
            boolean matchesTitleOrText = search.replaceAll("#\\w+", "").trim().isEmpty()
                    || p.title().contains(search.replaceAll("#\\w+", "").trim())
                    || p.text().contains(search.replaceAll("#\\w+", "").trim());
            boolean matchesTags = Stream.of(search.split(" ")).filter(w -> w.startsWith("#"))
                    .allMatch(tag -> p.tags().contains(tag.substring(1)));
            assertTrue(matchesTitleOrText && matchesTags);
        }
    }
}
