package io.github.habatoo.service.filenamegenerator;

import io.github.habatoo.service.FileNameGenerator;
import io.github.habatoo.service.impl.FileNameGeneratorImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Тесты для приватного метода getFileExtension
 */
class FileNameGeneratorGetFileExtensionTest extends FileNameGeneratorTestBase {

    @DisplayName("Должен извлекать расширение из файлов с различными форматами")
    @ParameterizedTest
    @MethodSource("fileExtensionProvider")
    void shouldExtractExtensionFromFilesWithExtensions(String filename, String expectedExtension) {
        String result = ReflectionTestUtils.invokeMethod(fileNameGenerator, "getFileExtension", filename);

        assertEquals(expectedExtension, result);
    }

    @DisplayName("Должен возвращать расширение по умолчанию для файлов без расширения")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"file", "noextension", "justname"})
    void shouldReturnDefaultExtensionForFilesWithoutExtension(String filename) {
        String result = ReflectionTestUtils.invokeMethod(fileNameGenerator, "getFileExtension", filename);

        assertEquals(DEFAULT_EXTENSION, result);
    }

    @DisplayName("Должен возвращать расширение по умолчанию для файлов с пустым расширением")
    @ParameterizedTest
    @ValueSource(strings = {"file.", "image.", "test."})
    void shouldReturnDefaultExtensionForEmptyExtension(String filename) {
        String result = ReflectionTestUtils.invokeMethod(fileNameGenerator, "getFileExtension", filename);

        assertEquals(DEFAULT_EXTENSION, result);
    }

    @DisplayName("Должен обрабатывать файлы с несколькими точками")
    @ParameterizedTest
    @MethodSource("multipleDotsFileProvider")
    void shouldHandleFilesWithMultipleDots(String filename, String expectedExtension) {
        String result = ReflectionTestUtils.invokeMethod(fileNameGenerator, "getFileExtension", filename);

        assertEquals(expectedExtension, result);
    }

    @DisplayName("Должен приводить расширения к нижнему регистру")
    @ParameterizedTest
    @MethodSource("uppercaseFileProvider")
    void shouldConvertExtensionToLowerCase(String filename, String expectedExtension) {
        String result = ReflectionTestUtils.invokeMethod(fileNameGenerator, "getFileExtension", filename);

        assertEquals(expectedExtension, result);
    }

    @DisplayName("Должен обрабатывать граничные случаи с точками")
    @ParameterizedTest
    @MethodSource("edgeCasesFileProvider")
    void shouldHandleEdgeCasesWithDots(String filename, String expectedExtension) {
        String result = ReflectionTestUtils.invokeMethod(fileNameGenerator, "getFileExtension", filename);

        assertEquals(expectedExtension, result);
    }

    @DisplayName("Должен работать с разными расширениями по умолчанию")
    @ParameterizedTest
    @MethodSource("defaultExtensionProvider")
    void shouldWorkWithDifferentDefaultExtensions(String defaultExt) {
        FileNameGenerator generator = new FileNameGeneratorImpl(defaultExt);

        String result = ReflectionTestUtils.invokeMethod(generator, "getFileExtension", "file");

        assertEquals(defaultExt, result);
    }
}
