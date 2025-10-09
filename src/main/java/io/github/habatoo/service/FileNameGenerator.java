package io.github.habatoo.service;

/**
 * Контракт для генерации уникальных имена файлов.
 */
public interface FileNameGenerator {

    /**
     * Генерирует уникальное имя файла на основе оригинального имени.
     * Гарантирует уникальность через временную метку и безопасность через валидацию расширения.
     *
     * @param originalFilename оригинальное имя файла
     * @return сгенерированное уникальное имя файла
     */
    String generateFileName(String originalFilename);

}
