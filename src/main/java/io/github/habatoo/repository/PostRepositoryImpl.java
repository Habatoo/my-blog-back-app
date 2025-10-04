package io.github.habatoo.repository;

import io.github.habatoo.model.Comment;
import io.github.habatoo.model.Post;
import io.github.habatoo.model.PostTag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Реализация репозитория для работы с постами блога.
 * Обеспечивает доступ к данным постов с использованием JDBC Template.
 *
 * <p>Данная реализация загружает посты вместе со связанными тегами и комментариями
 * в одном SQL запросе, используя JOIN операции для оптимизации производительности.</p>
 *
 * @see PostRepository
 * @see JdbcTemplate
 */
@Repository
public class PostRepositoryImpl implements PostRepository {

    private final JdbcTemplate jdbcTemplate;

    public PostRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Получает все посты из базы данных вместе с их тегами и комментариями.
     *
     * <p>Выполняет сложный SQL запрос с LEFT JOIN для загрузки связанных данных:
     * <ul>
     *   <li>Основная информация о посте</li>
     *   <li>Теги, связанные с постом через таблицу post_tag</li>
     *   <li>Комментарии, относящиеся к посту</li>
     * </ul>
     *
     * <p>Результаты сортируются по дате создания поста (от новых к старым),
     * затем по имени тега и дате создания комментария.</p>
     *
     * @return список всех постов с заполненными данными тегов и комментариев.
     * Если посты отсутствуют, возвращается пустой список
     */
    @Override
    public List<Post> findAll() {
        String sql = """
                SELECT
                    p.id as post_id, p.title, p.text, p.likes_count, p.comments_count,
                    p.image_url, p.image_name, p.image_size, p.created_at, p.updated_at,
                    t.id as tag_id, t.name as tag_name, t.created_at as tag_created_at,
                    c.id as comment_id, c.text as comment_text,
                    c.created_at as comment_created_at, c.updated_at as comment_updated_at
                FROM post p
                LEFT JOIN post_tag pt ON p.id = pt.post_id
                LEFT JOIN tag t ON pt.tag_id = t.id
                LEFT JOIN comment c ON p.id = c.post_id
                ORDER BY p.created_at DESC, t.name, c.created_at
                """;

        return jdbcTemplate.query(sql, new PostWithTagsAndCommentsRowMapper());
    }

    /**
     * Внутренний класс для маппинга результатов SQL запроса в объекты Post.
     * Обрабатывает отношения "один-ко-многим" для тегов и комментариев,
     * группируя связанные данные по идентификатору поста.</p>
     *
     * <p>Для оптимизации использует HashMap для отслеживания уже созданных постов
     * и предотвращения дублирования при JOIN операциях.</p>
     */
    private static class PostWithTagsAndCommentsRowMapper implements RowMapper<Post> {

        /**
         * Map для хранения уже созданных постов по их идентификаторам.
         * Используется для группировки связанных тегов и комментариев.
         */
        private final Map<Long, Post> postMap = new HashMap<>();

        /**
         * Преобразует текущую строку ResultSet в объект Post.
         *
         * <p>Метод обрабатывает следующие сценарии:
         * <ul>
         *   <li>Если пост с данным ID еще не создан - создает новый объект Post</li>
         *   <li>Если тег существует и не null - добавляет PostTag к посту</li>
         *   <li>Если комментарий существует и не null - добавляет Comment к посту</li>
         * </ul>
         *
         * @param rs     ResultSet с данными из базы данных
         * @param rowNum номер текущей строки в ResultSet
         * @return объект Post с заполненными данными тегов и комментариев
         * @throws SQLException в случае ошибок доступа к данным ResultSet
         */
        @Override
        public Post mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long postId = rs.getLong("post_id");

            Post post = postMap.get(postId);
            if (post == null) {
                post = Post.builder()
                        .id(postId)
                        .title(rs.getString("title"))
                        .text(rs.getString("text"))
                        .likesCount(rs.getInt("likes_count"))
                        .commentsCount(rs.getInt("comments_count"))
                        .imageUrl(rs.getString("image_url"))
                        .imageName(rs.getString("image_name"))
                        .imageSize(rs.getObject("image_size", Integer.class))
                        .createdAt(getLocalDateTime(rs, "created_at"))
                        .updatedAt(getLocalDateTime(rs, "updated_at"))
                        .tags(new HashSet<>())
                        .comments(new HashSet<>())
                        .build();
                postMap.put(postId, post);
            }

            // Добавление тега, если он существует в результате JOIN
            long tagId = rs.getLong("tag_id");
            if (!rs.wasNull() && tagId != 0) {
                PostTag postTag = new PostTag(
                        postId,
                        tagId,
                        getLocalDateTime(rs, "tag_created_at")
                );
                post.getTags().add(postTag);
            }

            // Добавление комментария, если он существует в результате JOIN
            long commentId = rs.getLong("comment_id");
            if (!rs.wasNull() && commentId != 0) {
                Comment comment = new Comment(
                        commentId,
                        postId,
                        rs.getString("comment_text"),
                        getLocalDateTime(rs, "comment_created_at"),
                        getLocalDateTime(rs, "comment_updated_at")
                );
                post.getComments().add(comment);
            }

            return post;
        }

        /**
         * Вспомогательный метод для преобразования Timestamp в LocalDateTime.
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
