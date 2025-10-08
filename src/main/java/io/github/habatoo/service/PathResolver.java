package io.github.habatoo.service;

import java.nio.file.Path;

/**
 * Контракт для безопасного разрешения и валидации путей файлов.
 */
public interface PathResolver {

    /**
     * Разрешает путь к файлу относительно базовой директории загрузок.
     * Выполняет проверку безопасности для предотвращения path traversal атак.
     *
     * @param filename имя файла для разрешения
     * @return абсолютный путь к файлу
     * @throws SecurityException если путь выходит за пределы разрешенной директории
     */
    Path resolveFilePath(String filename);

    /**
     * Разрешает путь к файлу в указанной директории.
     * Выполняет проверку безопасности для предотвращения path traversal атак.
     *
     * @param directory директория для размещения файла
     * @param filename  имя файла
     * @return абсолютный путь к файлу
     * @throws SecurityException если путь выходит за пределы разрешенной директории
     */
    Path resolveFilePath(Path directory, String filename);

}
