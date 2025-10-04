package io.github.habatoo.repository;

import io.github.habatoo.model.Comment;
import org.springframework.data.repository.Repository;

import java.util.List;

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
    List<Comment> findByPostId(Long postId);
}
