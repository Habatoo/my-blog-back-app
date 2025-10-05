package io.github.habatoo.repository;

import io.github.habatoo.dto.request.CommentCreateRequest;
import io.github.habatoo.dto.response.CommentResponse;
import io.github.habatoo.model.Comment;
import io.github.habatoo.repository.impl.CommentRepositoryImpl;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с комментариями блога.
 * Определяет контракты для операций доступа к данным комментариев.
 *
 * <p>Интерфейс расширяет Spring Data {@link Repository} и предоставляет
 * методы для извлечения комментариев, связанных с конкретными постами.</p>
 *
 * @see Repository
 * @see Comment
 * @see CommentRepositoryImpl
 */
public interface CommentRepository extends Repository<Comment, Long> {

    /**
     * Находит все комментарии, принадлежащие указанному посту.
     *
     * <p>Метод возвращает список комментариев, отсортированный по дате создания
     * в порядке убывания (от новых к старым).</p>
     *
     * @param postId идентификатор поста, для которого запрашиваются комментарии.
     *               Должен быть не-null и соответствовать существующему посту
     * @return список комментариев для указанного поста, отсортированный по
     * дате создания (новые первыми). Список может быть пустым, если
     * комментарии отсутствуют. Гарантируется, что возвращается
     * {@code List}, а не {@code null}
     * @see CommentRepositoryImpl#findByPostId(Long)
     */
    List<CommentResponse> findByPostId(Long postId);

    /**
     * Поиск комментария по идентификатору поста и комментария.
     * Проверяет принадлежность комментария указанному посту.
     *
     * @param postId    идентификатор поста, к которому принадлежит комментарий
     * @param commentId идентификатор комментария для поиска
     * @return {@code Optional} содержащий комментарий, если найден,
     * или пустой {@code Optional} если комментарий не существует
     * или не принадлежит указанному посту
     */
    Optional<CommentResponse> findByPostIdAndId(Long postId, Long commentId);

    /**
     * Сохранение нового комментария.
     *
     * @param commentCreateRequest объект комментария для сохранения
     * @return сохраненный комментарий с присвоенным идентификатором
     */
    CommentResponse save(CommentCreateRequest commentCreateRequest);

    /**
     * Обновление текста комментария и временной метки.
     *
     * @param postId    идентификатор поста для проверки принадлежности
     * @param commentId идентификатор комментария для обновления
     * @param text      новый текст комментария
     * @return обновленный комментарий с присвоенным идентификатором
     */
    Optional<CommentResponse> updateTextAndUpdatedAt(Long postId, Long commentId, String text);

    /**
     * Удаление комментария по идентификатору.
     *
     * @param postId    идентификатор поста для проверки принадлежности
     * @param commentId идентификатор комментария для удаления
     * @return true если комментарий найден и удален, false если не найден
     */
    boolean deleteById(Long postId, Long commentId);
}
