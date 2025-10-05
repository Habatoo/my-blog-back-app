package io.github.habatoo.repository;

import io.github.habatoo.dto.request.PostCreateRequest;
import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostListResponse;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.model.Post;
import io.github.habatoo.repository.impl.PostRepositoryImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.repository.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с постами блога.
 * Определяет контракты для операций доступа к данным постов.
 *
 * <p>Интерфейс расширяет Spring Data {@link Repository} и предоставляет
 * методы для извлечения постов вместе со связанными сущностями.</p>
 *
 * @see Repository
 * @see Post
 * @see PostRepositoryImpl
 */
public interface PostRepository extends Repository<Post, Long> {

    /**
     * Поиск постов с пагинацией и фильтрами по запросу.
     *
     * @param search     строка поиска
     * @param pageNumber номер страницы
     * @param pageSize   размер страницы
     * @return объект фильтрованного списка постов с пагинацие
     */
    PostListResponse findPostsWithPagination(String search, int pageNumber, int pageSize);

    /**
     * Реализация поиска поста по ID с полной информацией.
     * Загружает пост с полным текстом, тегами и комментариями.
     *
     * @param id идентификатор поста
     * @return Optional с постом если найден
     */
    Optional<PostResponse> findByIdWithFullContent(Long id);

    /**
     * Сохраняет новый пост в базе данных на основе данных из запроса.
     * Выполняет вставку основных данных поста и обрабатывает связанные теги.
     *
     * @param postCreateRequest объект запроса с данными для создания поста, содержащий
     *                          название, текст и список тегов
     * @return созданный объект {@link PostResponse} с присвоенным идентификатором и обработанными тегами
     * @throws IllegalStateException если исходный или сгенерированный ключ равен null
     * @throws DataAccessException   при ошибках доступа к базе данных
     */
    PostResponse save(PostCreateRequest postCreateRequest);

    /**
     * Обновляет существующий пост в базе данных на основе данных из запроса.
     * Модифицирует название, текст поста и временную метку обновления,
     * а также обрабатывает связанные теги.
     *
     * @param postRequest объект запроса с данными для обновления поста, содержащий
     *                    идентификатор, новое название, текст и список тегов
     * @return обновленный объект {@link PostResponse} с актуальными данными и тегами
     * @throws IllegalArgumentException       если запрос или ID null
     * @throws EmptyResultDataAccessException если пост с указанным ID не найден
     * @throws DataAccessException            при ошибках доступа к базе данных
     */
    PostResponse update(PostRequest postRequest);

    /**
     * Удаляет пост по идентификатору вместе со всеми связанными данными
     * включая файлы изображений на диске
     *
     * @param id идентификатор поста для удаления
     * @throws EmptyResultDataAccessException если пост с указанным идентификатором не найден
     * @throws DataRetrievalFailureException  если не удалось удалть файлы.папки изображений
     * @throws DataAccessException            при ошибках доступа к базе данных
     */
    void deleteById(Long id);

    /**
     * Увеличивает счетчик лайков поста на 1
     *
     * @param id идентификатор поста для увеличения лайков
     * @return обновленное количество лайков поста
     * @throws IllegalStateException          число лайков null
     * @throws EmptyResultDataAccessException если пост с указанным идентификатором не найден
     * @throws DataAccessException            при ошибках доступа к базе данных
     */
    int incrementLikes(Long id);

}
