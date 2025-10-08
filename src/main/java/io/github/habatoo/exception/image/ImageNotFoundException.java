package io.github.habatoo.exception.image;

import io.github.habatoo.exception.BusinessException;

public class ImageNotFoundException extends BusinessException {
    public ImageNotFoundException(Long postId) {
        super("Image not found for post with id: " + postId);
    }
}
