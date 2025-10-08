package io.github.habatoo.exception;

// Базовое бизнес-исключение
public abstract class BusinessException extends RuntimeException {
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(String message) {
        super(message);
    }
}
