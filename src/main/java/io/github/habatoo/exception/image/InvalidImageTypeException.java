package io.github.habatoo.exception.image;

import io.github.habatoo.exception.BusinessException;

public class InvalidImageTypeException extends BusinessException {
    public InvalidImageTypeException(String message) {
        super(message);
    }
}
