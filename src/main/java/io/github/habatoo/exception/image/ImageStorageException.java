package io.github.habatoo.exception.image;

import io.github.habatoo.exception.BusinessException;

public class ImageStorageException extends BusinessException {
    public ImageStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
