package io.github.habatoo.util;

import lombok.experimental.UtilityClass;

/**
 * Утилитный класс для работы с текстом.
 */
@UtilityClass
public final class TextUtils {

    /**
     * Обрезает текст до указанной длины и добавляет "…" если нужно.
     *
     * @param text текст для обрезки
     * @param maxLength максимальная длина текста
     * @return обрезанный текст
     */
    public static String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "…";
    }

    /**
     * Обрезает текст до 128 символов (стандартная длина для превью постов).
     */
    public static String truncatePostText(String text) {
        return truncateText(text, 128);
    }
}
