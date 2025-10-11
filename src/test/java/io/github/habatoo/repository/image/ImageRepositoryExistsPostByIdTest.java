package io.github.habatoo.repository.image;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.habatoo.repository.sql.ImageSqlQueries.CHECK_POST_EXISTS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * <h2>Тесты метода existsPostById в ImageRepository</h2>
 *
 * <p>
 * Класс покрывает все основные сценарии проверки существования поста по id:
 * <ul>
 *     <li>Пост с заданным id существует (ответ из базы = 1)</li>
 *     <li>Пост с заданным id не существует (ответ из базы = 0)</li>
 *     <li>Пост с заданным id отсутствует и jdbcTemplate возвращает null</li>
 * </ul>
 * Для тестирования используется мок JdbcTemplate.
 * Проверяется, что результат корректный для всех вариантов ответа базы.
 * </p>
 */
@DisplayName("Тесты existsPostById в ImageRepository")
public class ImageRepositoryExistsPostByIdTest extends ImageRepositoryTestBase {

    /**
     * Проверяет, что existsPostById возвращает true, если запрос к базе возвращает 1 (пост существует).
     */
    @Test
    @DisplayName("Должен вернуть true если пост с заданным id существует")
    void shouldReturnTrueIfPostExistsTest() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(existingPostId))).thenReturn(1);

        assertTrue(imageRepository.existsPostById(existingPostId));
        verify(jdbcTemplate).queryForObject(CHECK_POST_EXISTS, Integer.class, existingPostId);
    }

    /**
     * Проверяет, что existsPostById возвращает false, если в базе пост не найден (ответ 0).
     */
    @Test
    @DisplayName("Должен вернуть false если пост с заданным id не существует  ответ не null")
    void shouldReturnFalseIfPostDoesNotExistTest() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(nonExistingPostId))).thenReturn(0);

        assertFalse(imageRepository.existsPostById(nonExistingPostId));
        verify(jdbcTemplate).queryForObject(CHECK_POST_EXISTS, Integer.class, nonExistingPostId);
    }

    /**
     * Проверяет, что existsPostById возвращает false, если queryForObject возвращает null (нет такого поста).
     */
    @Test
    @DisplayName("Должен вернуть false если пост с заданным id не существует и ответ null")
    void shouldReturnFalseIfPostDoesNotExistNullAnswerTest() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(nonExistingPostId))).thenReturn(null);

        assertFalse(imageRepository.existsPostById(nonExistingPostId));
        verify(jdbcTemplate).queryForObject(CHECK_POST_EXISTS, Integer.class, nonExistingPostId);
    }
}
