package io.github.habatoo.repository.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.habatoo.repository.sql.PostSqlQueries.DECREMENT_COMMENTS_COUNT;
import static io.github.habatoo.repository.sql.PostSqlQueries.INCREMENT_COMMENTS_COUNT;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * <h2>Тесты инкремента и декремента счетчика комментариев в PostRepositoryImpl</h2>
 *
 * <p>
 * Класс покрывает корректность работы методов изменения количества комментариев у поста:
 * <ul>
 *     <li>incrementCommentsCount — увеличивает счетчик комментариев поста</li>
 *     <li>decrementCommentsCount — уменьшает счетчик комментариев поста</li>
 * </ul>
 * Для тестирования используется мок JdbcTemplate. Проверяется, что выполняется нужный SQL-запрос,
 * и метод не выбрасывает исключений при штатной ситуации.
 * </p>
 */
@DisplayName("Тесты метода incrementCommentsCount/decrementCommentsCount изменения количества комментариев.")
public class PostRepositoryIncrementDecrementCommentsTest extends PostRepositoryTestBase {

    /**
     * Проверяет, что при вызове incrementCommentsCount правильный SQL-запрос
     * выполняется и метод не выбрасывает исключений.
     */
    @Test
    @DisplayName("Должен увеличить счетчик комментариев")
    void shouldIncrementCommentsCountTest() {
        when(jdbcTemplate.update(INCREMENT_COMMENTS_COUNT, POST_ID)).thenReturn(1);

        assertDoesNotThrow(() -> postRepository.incrementCommentsCount(POST_ID));

        verify(jdbcTemplate).update(INCREMENT_COMMENTS_COUNT, POST_ID);
    }

    /**
     * Проверяет, что при вызове decrementCommentsCount правильный SQL-запрос
     * выполняется и метод не выбрасывает исключений.
     */
    @Test
    @DisplayName("Должен уменьшить счетчик комментариев")
    void shouldDecrementCommentsCountTest() {
        when(jdbcTemplate.update(DECREMENT_COMMENTS_COUNT, POST_ID)).thenReturn(1);

        assertDoesNotThrow(() -> postRepository.decrementCommentsCount(POST_ID));

        verify(jdbcTemplate).update(DECREMENT_COMMENTS_COUNT, POST_ID);
    }
}
