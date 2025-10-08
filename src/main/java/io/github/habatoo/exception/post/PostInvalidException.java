package io.github.habatoo.exception.post;

import io.github.habatoo.exception.BusinessException;

public class PostInvalidException extends BusinessException {
    public PostInvalidException() {
        super("Post ID must be not null or a positive number.");
    }
}
