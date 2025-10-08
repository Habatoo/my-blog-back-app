package io.github.habatoo.exception.image;

import io.github.habatoo.exception.BusinessException;

public class InvalidImageException extends BusinessException {
    public InvalidImageException(String message) {
        super(message);
    }
}
