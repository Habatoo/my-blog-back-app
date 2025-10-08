package io.github.habatoo.exception.post;

import io.github.habatoo.exception.BusinessException;

public class PostNotFoundException extends BusinessException {
    public PostNotFoundException(Long postId) {
        super("Post not found with id: " + postId);
    }
}
