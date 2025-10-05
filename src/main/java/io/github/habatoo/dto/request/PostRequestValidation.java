package io.github.habatoo.dto.request;

import java.util.List;

/**
 * Контракт на валидацию данных по постам.
 */
public interface PostRequestValidation {
    String title();
    String text();
    List<String> tags();
}
