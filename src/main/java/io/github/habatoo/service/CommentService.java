package io.github.habatoo.service;

import io.github.habatoo.dto.request.CommentRequest;
import io.github.habatoo.dto.response.CommentResponse;
import io.github.habatoo.model.Comment;
import io.github.habatoo.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с комментариями блога.
 * Предоставляет бизнес-логику для операций с комментариями к постам.
 *
 * <p>Сервис делегирует выполнение операций доступа к данным репозиторию
 * и обеспечивает работу с комментариями, связанных с конкретными постами.</p>
 *
 * @see CommentRepository
 * @see Comment
 */
@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * Получает все комментарии для указанного поста.
     *
     * <p>Возвращает список комментариев, связанных с конкретным постом.
     * Комментарии возвращаются в хронологическом порядке,
     * от новых к старым, как определено в реализации репозитория.</p>
     *
     * @param postId идентификатор поста, для которого запрашиваются комментарии.
     *               Должен быть валидным идентификатором существующего поста
     * @return список комментариев для указанного поста. Если комментарии отсутствуют
     * или пост с указанным идентификатором не существует, возвращается пустой список
     * @see CommentRepository#findByPostId(Long)
     */
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostId(postId);
    }

    /**
     * Получение комментария по идентификаторам поста и комментария.
     *
     * @param postId    идентификатор поста для проверки принадлежности
     * @param commentId идентификатор комментария для получения
     * @return {@code Optional} содержащий комментарий если найден и принадлежит посту,
     * иначе пустой {@code Optional}
     */
    public Optional<CommentResponse> getCommentByPostIdAndId(Long postId, Long commentId) {
        return commentRepository.findByPostIdAndId(postId, commentId);
    }

    /**
     * Создание нового комментария для указанного поста.
     * Устанавливает временные метки создания и обновления.
     *
     * @param commentRequest асррос содержит идентификатор поста и текст комментария
     * @return созданный комментарий с присвоенным идентификатором
     */
    public CommentResponse createComment(CommentRequest commentRequest) {
        return commentRepository.save(commentRequest);
    }

    /**
     * Обновление комментария по идентификаторам поста и комментария.
     * Проверяет принадлежность комментария посту перед обновлением.
     *
     * @param postId    идентификатор поста для проверки принадлежности
     * @param commentId идентификатор комментария для обновления
     * @param text      новый текст комментария
     * @return Optional с обновленным комментарием если найден и обновлен,
     * иначе пустой Optional
     */
    public Optional<CommentResponse> updateComment(Long postId, Long commentId, String text) {
        return commentRepository.updateTextAndUpdatedAt(postId, commentId, text);
    }

    /**
     * Удаление комментария по идентификаторам поста и комментария.
     * Проверяет принадлежность комментария посту перед удалением.
     *
     * @param postId    идентификатор поста для проверки принадлежности
     * @param commentId идентификатор комментария для удаления
     * @return true если комментарий найден и удален, false если не найден
     */
    public boolean deleteComment(Long postId, Long commentId) {
        return commentRepository.deleteById(postId, commentId);
    }

}
