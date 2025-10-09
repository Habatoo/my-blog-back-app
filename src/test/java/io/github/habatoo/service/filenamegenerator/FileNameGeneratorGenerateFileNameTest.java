package io.github.habatoo.service.filenamegenerator;

import io.github.habatoo.service.FileNameGenerator;
import io.github.habatoo.service.impl.FileNameGeneratorImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для метода generateFileName
 */
class FileNameGeneratorGenerateFileNameTest extends FileNameGeneratorTestBase {

    @DisplayName("Должен сгенерировать корректное имя файла для различных расширений")
    @ParameterizedTest
    @MethodSource("fileExtensionProvider")
    void shouldGenerateCorrectFileNameForDifferentExtensions(String originalFilename, String expectedExtension) {
        String result = fileNameGenerator.generateFileName(originalFilename);

        assertNotNull(result);
        String[] parts = result.split("_");
        assertEquals(2, parts.length);

        String timestamp = parts[0];
        String randomWithExt = parts[1];

        assertTrue(isValidTimestamp(timestamp));
        assertTrue(isValidRandom(randomWithExt.split("\\.")[0]));
        assertEquals(expectedExtension, extractExtension(result));
    }

    @DisplayName("Должен использовать расширение по умолчанию для файлов без расширения")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"file", "file.", "FILE", "  "})
    void shouldUseDefaultExtensionForFilesWithoutExtension(String originalFilename) {
        String result = fileNameGenerator.generateFileName(originalFilename);

        assertNotNull(result);
        assertEquals(DEFAULT_EXTENSION, extractExtension(result));
    }

    @DisplayName("Должен использовать расширение по умолчанию для файлов с пустым расширением")
    @ParameterizedTest
    @ValueSource(strings = {"file.", "image.", "test."})
    void shouldUseDefaultExtensionForEmptyExtension(String originalFilename) {
        String result = fileNameGenerator.generateFileName(originalFilename);

        assertEquals(DEFAULT_EXTENSION, extractExtension(result));
    }

    @DisplayName("Должен генерировать уникальные имена файлов")
    @Test
    void shouldGenerateUniqueFileNames() {
        String originalFilename = "image.jpg";

        String result1 = fileNameGenerator.generateFileName(originalFilename);
        String result2 = fileNameGenerator.generateFileName(originalFilename);

        assertNotNull(result1);
        assertNotNull(result2);
        assertTrue(!result1.equals(result2), "Generated filenames should be different");

        String random1 = extractRandom(result1);
        String random2 = extractRandom(result2);
        assertTrue(!random1.equals(random2), "Random parts should be different");
    }

    @DisplayName("Должен генерировать корректный формат имени файла")
    @Test
    void shouldGenerateCorrectFileNameFormat() {
        String originalFilename = "test.image.jpg";

        String result = fileNameGenerator.generateFileName(originalFilename);

        assertTrue(result.matches("^\\d+_\\d{4}\\.jpg$"),
                "Filename should match pattern: timestamp_random.jpg");

        String timestamp = extractTimestamp(result);
        String random = extractRandom(result);
        String extension = extractExtension(result);

        assertTrue(isValidTimestamp(timestamp));
        assertTrue(isValidRandom(random));
        assertEquals("jpg", extension);
    }

    @DisplayName("Должен обрабатывать файлы с точками в имени")
    @ParameterizedTest
    @MethodSource("multipleDotsFileProvider")
    void shouldHandleFilesWithDotsInName(String originalFilename, String expectedExtension) {
        String result = fileNameGenerator.generateFileName(originalFilename);

        assertEquals(expectedExtension, extractExtension(result));
    }

    @DisplayName("Должен генерировать имена с разными расширениями по умолчанию")
    @ParameterizedTest
    @MethodSource("defaultExtensionProvider")
    void shouldGenerateNamesWithDifferentDefaultExtensions(String defaultExt) {
        FileNameGenerator generator = new FileNameGeneratorImpl(defaultExt);

        String result = generator.generateFileName("file");

        assertEquals(defaultExt, extractExtension(result));
    }

    @DisplayName("Должен обрабатывать специальные символы в именах файлов")
    @ParameterizedTest
    @MethodSource("specialCharactersFileProvider")
    void shouldHandleSpecialCharactersInFilenames(String originalFilename, String expectedExtension) {

        String result = fileNameGenerator.generateFileName(originalFilename);

        assertEquals(expectedExtension, extractExtension(result));
        assertTrue(isValidTimestamp(extractTimestamp(result)));
        assertTrue(isValidRandom(extractRandom(result)));
    }
}
