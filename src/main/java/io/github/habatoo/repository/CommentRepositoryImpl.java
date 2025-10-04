package io.github.habatoo.repository;

import io.github.habatoo.model.Comment;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
     * Реализация поиска комментария по идентификатору поста и комментария.
     * Выполняет SQL запрос с условием по post_id и id для гарантии принадлежности.
     *
     * @param postId    идентификатор поста для проверки принадлежности
     * @param commentId идентификатор комментария для извлечения
     * @return {@code Optional} с комментарием если найден, иначе пустой {@code Optional}
     */
    @Override
    public Optional<Comment> findByPostIdAndId(
            Long postId,
            Long commentId) {
        String sql = """
                SELECT
                    c.id, c.post_id, c.text, c.created_at, c.updated_at
                FROM comment c
                INNER JOIN post p ON c.post_id = p.id
                WHERE p.id = ? AND c.id = ?
                """;

        List<Comment> comments = jdbcTemplate.query(sql, new CommentRowMapper(), postId, commentId);
        return comments.stream().findFirst();
    }

    /**
     * Реализация сохранения комментария в базу данных.
     * Выполняет вставку комментария и атомарно увеличивает счетчик комментариев у поста.
     * Гарантирует целостность данных в рамках транзакции.
     *
     * @param comment комментарий для сохранения, должен содержать валидные postId, text и временные метки
     * @return сохраненный комментарий с присвоенным идентификатором
     * @throws DataIntegrityViolationException если пост с указанным postId не существует
     * @throws DataAccessException             при ошибках доступа к базе данных
     */
    @Transactional
    @Override
    public Comment save(Comment comment) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO comment (post_id, text, created_at, updated_at) VALUES (?, ?, ?, ?)",
                    new String[]{"id"}
            );
            ps.setLong(1, comment.getPostId());
            ps.setString(2, comment.getText());
            ps.setTimestamp(3, Timestamp.valueOf(comment.getCreatedAt()));
            ps.setTimestamp(4, Timestamp.valueOf(comment.getUpdatedAt()));
            return ps;
        }, keyHolder);

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        comment.setId(id);

        jdbcTemplate.update(
                "UPDATE post SET comments_count = comments_count + 1, updated_at = ? WHERE id = ?",
                LocalDateTime.now(),
                comment.getPostId()
        );

        return comment;
    }

    /**
     * Реализация обновления комментария в базе данных.
     * Обновляет текст и время последнего изменения комментария.
     *
     * @param postId    идентификатор поста для проверки принадлежности
     * @param commentId идентификатор комментария для обновления
     * @param text      новый текст комментария
     * @return обновленный комментарий
     */
    @Transactional
    @Override
    public Optional<Comment> updateTextAndUpdatedAt(
            Long postId,
            Long commentId,
            String text) {
        jdbcTemplate.update(
                "UPDATE comment SET text = ?, updated_at = ? WHERE id = ?",
                text,
                LocalDateTime.now(),
                commentId
        );

        return findByPostIdAndId(postId, commentId);
    }

    /**
     * Удаление комментария с проверкой принадлежности посту и обновлением счетчика.
     * Атомарная операция в рамках транзакции.
     *
     * @param postId    идентификатор поста для проверки принадлежности
     * @param commentId идентификатор комментария для удаления
     * @return true если комментарий удален, false если не найден
     */
    @Transactional
    @Override
    public boolean deleteById(Long postId, Long commentId) {
        int deletedRows = jdbcTemplate.update(
                "DELETE FROM comment WHERE id = ? AND post_id = ?",
                commentId,
                postId
        );

        boolean deleteResult = deletedRows > 0;

        if (deleteResult) {
            jdbcTemplate.update(
                    "UPDATE post SET comments_count = comments_count - 1, updated_at = ? WHERE id = ?",
                    LocalDateTime.now(),
                    postId
            );
        }

        return deleteResult;
    }

    /**
     * Внутренний класс для маппинга результатов SQL запроса в объекты Comment.
     *
     * <p>Реализует интерфейс {@link RowMapper} для преобразования каждой строки
     * ResultSet в объект {@link Comment}.</p>
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
