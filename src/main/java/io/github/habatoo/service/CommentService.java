package io.github.habatoo.service;

import io.github.habatoo.model.Comment;
import io.github.habatoo.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostId(postId);
    }
}
