package io.github.habatoo.repository.impl;

import io.github.habatoo.controller.dto.PostListResponse;
import io.github.habatoo.controller.dto.PostRequest;
import io.github.habatoo.controller.dto.PostResponse;
import io.github.habatoo.model.Comment;
import io.github.habatoo.model.Post;
import io.github.habatoo.model.PostTag;
import io.github.habatoo.model.Tag;
import io.github.habatoo.repository.PostRepository;
import io.github.habatoo.repository.PostTagRepository;
import io.github.habatoo.repository.TagRepository;
import io.github.habatoo.service.FileStorageService;
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
import java.util.*;

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
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final FileStorageService fileStorageService;

    public PostRepositoryImpl(
            JdbcTemplate jdbcTemplate,
            TagRepository tagRepository,
            PostTagRepository postTagRepository,
            FileStorageService fileStorageService) {
        this.jdbcTemplate = jdbcTemplate;
        this.tagRepository = tagRepository;
        this.postTagRepository = postTagRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * TODO
     *
     * @param search     строка поиска
     * @param pageNumber номер страницы
     * @param pageSize   размер страницы
     * @return
     */
    @Override
    public PostListResponse findPostsWithPagination(String search, int pageNumber, int pageSize) {
        // Валидация параметров
        if (search == null) {
            throw new IllegalArgumentException("Search parameter cannot be null");
        }
        if (pageNumber < 1) {
            throw new IllegalArgumentException("Page number must be greater than 0");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }

        // Расчет смещения
        int offset = (pageNumber - 1) * pageSize;

        try {
            // Запрос для подсчета
            String countSql = """
                    SELECT COUNT(DISTINCT p.id) 
                    FROM post p
                    LEFT JOIN post_tag pt ON p.id = pt.post_id
                    LEFT JOIN tag t ON pt.tag_id = t.id
                    WHERE p.title LIKE ? 
                       OR p.text LIKE ?
                       OR t.name LIKE ?
                    """;

            Integer totalCount = jdbcTemplate.queryForObject(
                    countSql,
                    Integer.class,
                    "%" + search + "%", "%" + search + "%", "%" + search + "%"
            );

            if (totalCount == null || totalCount == 0) {
                return new PostListResponse(Collections.emptyList(), false, false, 0);
            }

            // Расчет пагинации
            int lastPage = (int) Math.ceil((double) totalCount / pageSize);
            boolean hasPrev = pageNumber > 1;
            boolean hasNext = pageNumber < lastPage;

            // Запрос для получения постов
            String postsSql = """
                    SELECT DISTINCT
                        p.id, 
                        p.title, 
                        p.text, 
                        p.likes_count, 
                        p.comments_count,
                        p.created_at
                    FROM post p
                    LEFT JOIN post_tag pt ON p.id = pt.post_id
                    LEFT JOIN tag t ON pt.tag_id = t.id
                    WHERE p.title LIKE ? 
                       OR p.text LIKE ?
                       OR t.name LIKE ?
                    ORDER BY p.created_at DESC
                    LIMIT ? OFFSET ?
                    """;

            // Получаем посты без тегов
            List<PostResponse> posts = jdbcTemplate.query(
                    postsSql,
                    new PostListRowMapper(),
                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
                    pageSize, offset
            );

            // Заполняем теги отдельно для каждого поста
            for (PostResponse post : posts) {
                List<String> tags = getTagsForPost(post.getId());
                post.setTags(tags);
            }

            return new PostListResponse(posts, hasPrev, hasNext, lastPage);

        } catch (Exception e) {
            // Детальное логирование для диагностики
            System.err.printf("Error in findPostsWithPagination: %s \n", e);
            System.err.printf("Search: %s, Page: %d, Size: %d \n", search, pageNumber, pageSize);

            // Проверим конкретный тип исключения
            if (e instanceof DataAccessException) {
                System.err.printf("SQL State: %s \n", ((DataAccessException) e).getRootCause());
            }
            throw new DataAccessException("Database error while searching posts", e) {
            };
        }
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
     * Реализация поиска поста по ID с полной информацией.
     * Загружает пост с полным текстом, тегами и комментариями.
     *
     * @param id идентификатор поста
     * @return Optional с постом если найден
     */
    @Override
    public Optional<Post> findByIdWithFullContent(Long id) {
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
                WHERE p.id = ?
                ORDER BY t.name, c.created_at
                """;

        List<Post> posts = jdbcTemplate.query(sql, new PostWithTagsAndCommentsRowMapper(), id);
        return posts.isEmpty() ? Optional.empty() : Optional.of(posts.getFirst());
    }

    /**
     * Сохраняет новый пост в базе данных на основе данных из запроса.
     * Выполняет вставку основных данных поста и обрабатывает связанные теги.
     *
     * @param postRequest объект запроса с данными для создания поста, содержащий
     *                    название, текст и список тегов
     * @return созданный объект {@link Post} с присвоенным идентификатором и обработанными тегами
     * @throws IllegalStateException если исходный или сгенерированный ключ равен null
     * @throws DataAccessException   при ошибках доступа к базе данных
     */
    @Override
    @Transactional
    public Post save(PostRequest postRequest) {
        if (postRequest == null) {
            throw new IllegalArgumentException("PostRequest cannot be null");
        }
        if (postRequest.getTitle() == null || postRequest.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Post title cannot be null or empty");
        }

        final String sql = "INSERT INTO post (title, text) VALUES (?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, postRequest.getTitle());
            ps.setString(2, postRequest.getText());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to generate primary key for new post");
        }

        Long generatedId = key.longValue();
        Set<PostTag> postTags = processPostTags(generatedId, postRequest.getTags());

        return Post.builder()
                .id(generatedId)
                .title(postRequest.getTitle())
                .text(postRequest.getText())
                .tags(postTags)
                .build();
    }

    /**
     * Обновляет существующий пост в базе данных на основе данных из запроса.
     * Модифицирует название, текст поста и временную метку обновления,
     * а также обрабатывает связанные теги.
     *
     * @param postRequest объект запроса с данными для обновления поста, содержащий
     *                    идентификатор, новое название, текст и список тегов
     * @return обновленный объект {@link Post} с актуальными данными и тегами
     * @throws IllegalArgumentException       если запрос или ID null
     * @throws EmptyResultDataAccessException если пост с указанным ID не найден
     * @throws DataAccessException            при ошибках доступа к базе данных
     */
    @Override
    @Transactional
    public Post update(PostRequest postRequest) {
        if (postRequest == null) {
            throw new IllegalArgumentException("PostRequest cannot be null");
        }
        if (postRequest.getId() == null) {
            throw new IllegalArgumentException("Post ID cannot be null");
        }

        Long id = postRequest.getId();
        Optional<Post> existingPost = findByIdWithFullContent(id);
        if (existingPost.isEmpty()) {
            throw new EmptyResultDataAccessException("Post with id " + id + " not found", 1);
        }

        final String sql = "UPDATE post SET title = ?, text = ?, updated_at = ? WHERE id = ?";
        int updatedRows = jdbcTemplate.update(
                sql,
                postRequest.getTitle(),
                postRequest.getText(),
                Timestamp.valueOf(LocalDateTime.now()),
                id
        );

        if (updatedRows == 0) {
            throw new DataAccessException("Post was concurrently modified or deleted") {
            };
        }

        Set<PostTag> postTags = processPostTags(id, postRequest.getTags());
        Integer likesCount = existingPost.map(Post::getLikesCount).orElse(0);
        Integer commentsCount = existingPost.map(Post::getCommentsCount).orElse(0);

        return Post.builder()
                .id(id)
                .title(postRequest.getTitle())
                .text(postRequest.getText())
                .likesCount(likesCount)
                .commentsCount(commentsCount)
                .tags(postTags)
                .build();
    }

    /**
     * Удаляет пост по идентификатору вместе со всеми связанными данными
     * включая файлы изображений на диске
     *
     * @param id идентификатор поста для удаления
     * @throws EmptyResultDataAccessException если пост с указанным идентификатором не найден
     * @throws DataRetrievalFailureException  если не удалось удалть файлы.папки изображений
     * @throws DataAccessException            при ошибках доступа к базе данных
     */
    @Override
    @Transactional
    public void deleteById(Long id) {
        String imageFileName = getImageFileName(id);

        final String sql = "DELETE FROM post WHERE id = ?";
        int affectedRows = jdbcTemplate.update(sql, id);

        if (affectedRows == 0) {
            throw new EmptyResultDataAccessException("Post with id " + id + " not found", 1);
        }

        if (imageFileName != null) {
            try {
                fileStorageService.deleteImageFile(imageFileName);
                fileStorageService.deletePostDirectory(id);
            } catch (Exception e) {
                throw new DataRetrievalFailureException("Failed to delete image file for post " + id, e);
            }
        }
    }

    /**
     * Увеличивает счетчик лайков поста на 1
     *
     * @param id идентификатор поста для увеличения лайков
     * @return обновленное количество лайков поста
     * @throws IllegalStateException          число лайков null
     * @throws EmptyResultDataAccessException если пост с указанным идентификатором не найден
     * @throws DataAccessException            при ошибках доступа к базе данных
     */
    @Override
    @Transactional
    public int incrementLikes(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Post ID cannot be null");
        }

        final String updateSql = "UPDATE post SET likes_count = likes_count + 1 WHERE id = ?";
        int updatedRows = jdbcTemplate.update(updateSql, id);

        if (updatedRows == 0) {
            throw new EmptyResultDataAccessException("Post with id " + id + " not found", 1);
        }

        Integer likesCount = jdbcTemplate.queryForObject(
                "SELECT likes_count FROM post WHERE id = ?",
                Integer.class,
                id
        );

        if (likesCount == null) {
            throw new IllegalStateException("Likes count is null for post with id " + id);
        }

        return likesCount;
    }

    /**
     * Обрабатывает теги поста: создает или находит существующие теги и создает связи.
     * Для новых постов создает связи, для обновления - заменяет старые теги новыми.
     *
     * @param postId   идентификатор поста для привязки тегов
     * @param tagNames список имен тегов для обработки, может быть null или пустым
     * @return набор связей {@link PostTag} между постом и тегами
     * @throws IllegalStateException если postId null или в списке тэгов null
     * @throws DataAccessException   при ошибках доступа к базе данных
     */
    private Set<PostTag> processPostTags(
            Long postId,
            List<String> tagNames) {
        if (postId == null) {
            throw new IllegalArgumentException("Post ID cannot be null");
        }

        if (tagNames == null || tagNames.isEmpty()) {
            jdbcTemplate.update("DELETE FROM post_tag WHERE post_id = ?", postId);
            return new HashSet<>();
        }

        if (tagNames.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Tag names list cannot contain null values");
        }

        jdbcTemplate.update("DELETE FROM post_tag WHERE post_id = ?", postId);

        Set<PostTag> postTags = new HashSet<>();
        LocalDateTime now = LocalDateTime.now();

        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(tagName));

            PostTag postTag = new PostTag(postId, tag.getId(), now);
            postTagRepository.save(postTag);
            postTags.add(postTag);
        }

        return postTags;
    }

    /**
     * Получает теги для конкретного поста
     */
    private List<String> getTagsForPost(Long postId) {
        try {
            String tagsSql = "SELECT t.name FROM tag t JOIN post_tag pt ON t.id = pt.tag_id WHERE pt.post_id = ?";

            return jdbcTemplate.query(
                    tagsSql,
                    (rs, rowNum) -> rs.getString("name"),
                    postId
            );
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * RowMapper для маппинга результатов в PostResponse
     */
    private static class PostListRowMapper implements RowMapper<PostResponse> {

        @Override
        public PostResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            String title = rs.getString("title");
            String fullText = rs.getString("text");

            // Обрезаем текст до 128 символов если нужно
            String truncatedText = truncateText(fullText);

            Integer likesCount = rs.getInt("likes_count");
            Integer commentsCount = rs.getInt("comments_count");

            // Теги будут заполнены позже отдельным запросом
            // В текущем запросе нет столбца "tags"
            List<String> tags = Collections.emptyList();

            return new PostResponse(id, title, truncatedText, tags, likesCount, commentsCount);
        }

        /**
         * Обрезает текст до 128 символов и добавляет "…" если нужно
         */
        private String truncateText(String text) {
            if (text == null) return "";
            if (text.length() <= 128) return text;

            return text.substring(0, 128) + "…";
        }
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
            if (timestamp == null || rs.wasNull()) {
                return null;
            }
            return timestamp.toLocalDateTime();
        }
    }

    /**
     * Получает имя файла изображения для поста
     *
     * @param postId идентификатор поста
     * @return имя файла или null если изображение не установлено
     */
    private String getImageFileName(Long postId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT image_url FROM post WHERE id = ?",
                    String.class,
                    postId
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
