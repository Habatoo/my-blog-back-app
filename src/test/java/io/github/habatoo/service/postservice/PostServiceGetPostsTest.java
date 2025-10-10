package io.github.habatoo.service.postservice;

import io.github.habatoo.dto.response.PostListResponse;
import io.github.habatoo.dto.response.PostResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Тесты метода getPosts класса PostServiceImpl
 */
@DisplayName("Тесты метода getPosts")
class PostServiceGetPostsTest extends PostServiceTestBase {

    @ParameterizedTest(name = "Фильтр \"{0}\", ожидаем количество {1}")
    @MethodSource("postsForFiltering")
    @DisplayName("Должен возвращать отфильтрованный и пагинированный список постов")
    void shouldReturnFilteredPagedPosts(String filter, int expectedCount) {
        PostListResponse result = postService.getPosts(filter, 1, 10);

        assertEquals(expectedCount, result.posts().size());
        assertFalse(result.hasPrev());
        assertEquals(expectedCount < 10 && expectedCount > 0, result.hasNext());
        assertEquals(expectedCount, result.posts().size());
    }

    @ParameterizedTest(name = "Поиск \"{0}\", страница {1}, размер {2}: ожидается {4} постов")
    @MethodSource("postsForFilteringWithPaging")
    @DisplayName("Должен корректно фильтровать, сортировать и возвращать страницы постов")
    void shouldReturnFilteredAndPaginatedPosts(String search, int pageNumber, int pageSize,
                                               int expectedCount, boolean hasPrev, boolean hasNext) {
        PostListResponse result = postService.getPosts(search, pageNumber, pageSize);

        assertEquals(expectedCount, result.posts().size());
        assertEquals(hasPrev, result.hasPrev());
        assertEquals(hasNext, result.hasNext());
        assertEquals(expectedCount, result.posts().size());
    }

    @ParameterizedTest(name = "Поиск \"{0}\", страница {1}, размер {2}")
    @MethodSource("postsForFilteringWithPaging")
    @DisplayName("Должен возвращать посты, сортированные по id по возрастанию")
    void shouldReturnPostsSortedById(String search, int pageNumber, int pageSize,
                                     int expectedCount, boolean hasPrev, boolean hasNext) {
        PostListResponse result = postService.getPosts(search, pageNumber, pageSize);
        List<Long> ids = result.posts().stream().map(PostResponse::id).toList();
        List<Long> sorted = new ArrayList<>(ids);
        Collections.sort(sorted);
        assertEquals(sorted, ids);
    }

    static Stream<Arguments> postsForFilteringWithPaging() {
        List<PostResponse> manyPosts = new ArrayList<>();
        for (long i = 1; i <= 25; i++) {
            manyPosts.add(new PostResponse(i, "Заголовок " + i, "Текст " + i,
                    i % 2 == 0 ? List.of("tag1", "tag2") : List.of("tag2"), (int) i, (int) i * 2));
        }

        return Stream.of(
                // Пустой поиск и теги - возвращается весь кеш (3 поста в базовом)
                Arguments.of("", 1, 10, 3, false, false),
                Arguments.of("", 2, 10, 0, true, false),

                // Поиск одного слова, пустой тег
                Arguments.of("Первый", 1, 10, 1, false, false),
                Arguments.of("Второй", 1, 10, 1, false, false),

                // Несколько слов, пустой тег
                Arguments.of("Второй текст", 1, 10, 1, false, false),

                // Пустой тег с одним словом
                Arguments.of("#tag1", 1, 10, 1, false, false),
                Arguments.of("#tag2", 1, 10, 2, false, false),
                Arguments.of("#tag3", 1, 10, 1, false, false),
                Arguments.of("#unknowntag", 1, 10, 0, false, false),

                // Пагинация с большим числом постов, полный кеш из manyPosts
                Arguments.of("","1", 1, 10, 25, false, true, manyPosts),
                Arguments.of("","2", 2, 10, 25, true, true, manyPosts),
                Arguments.of("","3", 3, 10, 25, true, true, manyPosts),
                Arguments.of("","4", 4, 10, 25, true, false, manyPosts),

                // Тест пустого поискового слова, фильтр по тегу один
                Arguments.of("#tag1", 1, 10, 12, false, true, manyPosts),
                Arguments.of("#tag2", 1, 10, 25, false, true, manyPosts),

                // Поиск с несколькими словами и тегом
                Arguments.of("Заголовок #tag1", 1, 10, 12, false, true, manyPosts)
        ).map(args -> {
            // Маппим строки пагинации к int
            Object[] paramArr = args.get();
            if (paramArr.length == 6) {
                return Arguments.of(paramArr[0], Integer.parseInt(paramArr[1].toString()), paramArr[2], paramArr[3], paramArr[4], paramArr[5], POST_RESPONSE_1, POST_RESPONSE_2, POST_RESPONSE_3);
            } else if (paramArr.length == 7) {
                return args;
            } else {
                return args;
            }
        });
    }
}
