package io.github.habatoo.repository;

import io.github.habatoo.model.Comment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Реализация репозитория для работы с комментариями блога.
 * Обеспечивает доступ к данным комментариев с использованием JDBC Template.
 *
 * @see CommentRepository
 * @see JdbcTemplate
 */
@Repository
public class CommentRepositoryImpl implements CommentRepository {

    private final JdbcTemplate jdbcTemplate;

    public CommentRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Находит все комментарии, связанные с указанным постом.
     *
     * <p>Выполняет SQL запрос для выборки комментариев по идентификатору поста.
     * Результаты сортируются по дате создания в порядке убывания (от новых к старым).</p>
     *
     * @param postId идентификатор поста, для которого запрашиваются комментарии.
     *               Должен быть не-null и соответствовать существующему посту
     * @return список комментариев для указанного поста, отсортированный по дате
     * создания (новые первыми). Если комментарии отсутствуют, возвращается
     * пустой список
     */
    @Override
    public List<Comment> findByPostId(Long postId) {
        String sql = """
                SELECT
                    id, post_id, text, created_at, updated_at
                FROM comment
                WHERE post_id = ?
                ORDER BY created_at DESC
                """;

        return jdbcTemplate.query(sql, new CommentRowMapper(), postId);
    }

    /**
     * Внутренний класс для маппинга результатов SQL запроса в объекты Comment.
     *
     * <p>Реализует интерфейс {@link RowMapper} для преобразования каждой строки
     * ResultSet в объект {@link Comment}. Использует паттерн Builder для создания
     * объектов комментариев, что обеспечивает читаемость и гибкость кода.</p>
     */
    private static class CommentRowMapper implements RowMapper<Comment> {

        /**
         * Преобразует текущую строку ResultSet в объект Comment.
         *
         * @param rs     ResultSet с данными из базы данных
         * @param rowNum номер текущей строки в ResultSet
         * @return объект Comment с заполненными данными
         * @throws SQLException в случае ошибок доступа к данным ResultSet
         */
        @Override
        public Comment mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Comment.builder()
                    .id(rs.getLong("id"))
                    .postId(rs.getLong("post_id"))
                    .text(rs.getString("text"))
                    .createdAt(getLocalDateTime(rs, "created_at"))
                    .updatedAt(getLocalDateTime(rs, "updated_at"))
                    .build();
        }

        /**
         * Вспомогательный метод для безопасного преобразования Timestamp в LocalDateTime.
         *
         * @param rs         ResultSet для получения данных
         * @param columnName имя колонки с временной меткой
         * @return LocalDateTime или null, если значение в колонке null
         * @throws SQLException в случае ошибок доступа к данным
         */
        private LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws SQLException {
            Timestamp timestamp = rs.getTimestamp(columnName);
            return timestamp != null ? timestamp.toLocalDateTime() : null;
        }
    }
}
