package io.github.habatoo.repository.impl;

import io.github.habatoo.dto.request.CommentRequest;
import io.github.habatoo.dto.response.CommentResponse;
import io.github.habatoo.repository.CommentRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
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

    public CommentRepositoryImpl(
            JdbcTemplate jdbcTemplate) {
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
     * @throws IllegalArgumentException если postId не передан или null
     */
    @Override
    public List<CommentResponse> findByPostId(Long postId) {
        if (postId == null) {
            throw new IllegalArgumentException("Post ID cannot be null");
        }

        String sql = """
                SELECT id, post_id, text
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
     * @throws IllegalArgumentException если postId/commentId не передан или null
     */
    @Override
    public Optional<CommentResponse> findByPostIdAndId(Long postId, Long commentId) {
        if (postId == null) {
            throw new IllegalArgumentException("Post ID cannot be null");
        }
        if (commentId == null) {
            throw new IllegalArgumentException("Comment ID cannot be null");
        }

        String sql = """
                SELECT c.id, c.post_id, c.text
                FROM comment c
                INNER JOIN post p ON c.post_id = p.id
                WHERE p.id = ? AND c.id = ?
                """;

        List<CommentResponse> comments = jdbcTemplate.query(sql, new CommentRowMapper(), postId, commentId);
        return comments.stream().findFirst();
    }

    /**
     * Реализация сохранения комментария в базу данных.
     * Выполняет вставку комментария и атомарно увеличивает счетчик комментариев у поста.
     * Гарантирует целостность данных в рамках транзакции.
     *
     * @param commentRequest комментарий для сохранения, должен содержать валидные postId, text и временные метки
     * @return сохраненный комментарий с присвоенным идентификатором
     * @throws EmptyResultDataAccessException если пост с указанным postId не существует
     * @throws DataRetrievalFailureException  если после обновления не сгенерирован postId
     * @throws DataAccessException            при ошибках доступа к базе данных
     */
    @Transactional
    @Override
    public CommentResponse save(CommentRequest commentRequest) {
        Long postId = commentRequest.postId();
        String text = commentRequest.text();

        if (!postExists(postId)) {
            throw new EmptyResultDataAccessException("Post with id " + postId + " not found", 1);
        }

        Long commentId = insertComment(postId, text);

        updatePostCommentsCount(postId, 1);

        return new CommentResponse(commentId, text, postId);
    }

    /**
     * Реализация обновления комментария в базе данных.
     * Обновляет текст и время последнего изменения комментария.
     *
     * @param postId    идентификатор поста для проверки принадлежности
     * @param commentId идентификатор комментария для обновления
     * @param text      новый текст комментария
     * @return обновленный комментарий
     * @throws IllegalArgumentException если postId/commentId не передан или null
     * @throws DataAccessException      при ошибках доступа к базе данных
     */
    @Transactional
    @Override
    public Optional<CommentResponse> updateTextAndUpdatedAt(Long postId, Long commentId, String text) {
        if (postId == null || commentId == null) {
            throw new IllegalArgumentException("Post ID and Comment ID cannot be null");
        }
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment text cannot be null or empty");
        }

        if (!commentExists(postId, commentId)) {
            return Optional.empty();
        }

        int updatedRows = jdbcTemplate.update(
                "UPDATE comment SET text = ?, updated_at = ? WHERE id = ? AND post_id = ?",
                text,
                Timestamp.valueOf(LocalDateTime.now()),
                commentId,
                postId
        );

        if (updatedRows == 0) {
            return Optional.empty();
        }

        return Optional.of(new CommentResponse(commentId, text, postId));
    }

    /**
     * Удаление комментария с проверкой принадлежности посту и обновлением счетчика.
     * Атомарная операция в рамках транзакции.
     *
     * @param postId    идентификатор поста для проверки принадлежности
     * @param commentId идентификатор комментария для удаления
     * @return true если комментарий удален, false если не найден
     * @throws IllegalArgumentException       если postId/commentId не передан или null
     * @throws EmptyResultDataAccessException если комментарий  или пост с указанными id не существуют
     * @throws DataRetrievalFailureException  если обновление не прошло
     * @throws DataAccessException            при ошибках доступа к базе данных
     */
    @Transactional
    @Override
    public boolean deleteById(Long postId, Long commentId) {
        if (postId == null || commentId == null) {
            throw new IllegalArgumentException("Post ID and Comment ID cannot be null");
        }

        if (!commentExists(postId, commentId)) {
            throw new EmptyResultDataAccessException("Comment not found", 1);
        }

        int deletedRows = jdbcTemplate.update(
                "DELETE FROM comment WHERE id = ? AND post_id = ?",
                commentId,
                postId
        );

        if (deletedRows > 0) {
            updatePostCommentsCount(postId, -1);
            return true;
        }

        return false;
    }

    /**
     * Вставляет комментарий в базу и возвращает сгенерированный ID
     */
    private Long insertComment(Long postId, String text) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO comment (post_id, text, created_at, updated_at) VALUES (?, ?, ?, ?)",
                    new String[]{"id"}
            );
            ps.setLong(1, postId);
            ps.setString(2, text);
            LocalDateTime now = LocalDateTime.now();
            ps.setTimestamp(3, Timestamp.valueOf(now));
            ps.setTimestamp(4, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new DataRetrievalFailureException("Failed to retrieve generated key for comment");
        }

        return key.longValue();
    }

    /**
     * Обновляет счетчик комментариев поста
     */
    private void updatePostCommentsCount(Long postId, int delta) {
        int updatedRows = jdbcTemplate.update(
                "UPDATE post SET comments_count = comments_count + ?, updated_at = ? WHERE id = ?",
                delta,
                Timestamp.valueOf(LocalDateTime.now()),
                postId
        );

        if (updatedRows == 0) {
            throw new DataAccessException("Failed to update post comments count") {
            };
        }
    }

    /**
     * Проверяет существование поста
     */
    private boolean postExists(Long postId) {
        String sql = "SELECT COUNT(*) FROM post WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, postId);
        return count != null && count > 0;
    }

    /**
     * Проверяет существование комментария
     */
    private boolean commentExists(Long postId, Long commentId) {
        String sql = "SELECT COUNT(*) FROM comment WHERE id = ? AND post_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, commentId, postId);
        return count != null && count > 0;
    }

    /**
     * Внутренний класс для маппинга результатов SQL запроса в объекты CommentResponse.
     *
     * <p>Реализует интерфейс {@link RowMapper} для преобразования каждой строки
     * ResultSet в объект {@link CommentResponse}.</p>
     */
    private static class CommentRowMapper implements RowMapper<CommentResponse> {

        @Override
        public CommentResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new CommentResponse(
                    rs.getLong("id"),
                    rs.getString("text"),
                    rs.getLong("post_id")
            );

        }

    }

}
