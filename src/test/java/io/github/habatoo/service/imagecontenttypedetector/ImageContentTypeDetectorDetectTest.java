package io.github.habatoo.service.imagecontenttypedetector;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для метода detect
 */
class ImageContentTypeDetectorDetectTest extends ImageContentTypeDetectorTestBase {

    @DisplayName("Должен определить JPEG формат для валидных данных")
    @Test
    void shouldDetectJpegForValidData() {
        byte[] jpegData = createJpegData();

        MediaType result = contentTypeDetector.detect(jpegData);

        assertEquals(MediaType.IMAGE_JPEG, result);
    }

    @DisplayName("Должен определить PNG формат для валидных данных")
    @Test
    void shouldDetectPngForValidData() {
        byte[] pngData = createPngData();

        MediaType result = contentTypeDetector.detect(pngData);

        assertEquals(MediaType.IMAGE_PNG, result);
    }

    @DisplayName("Должен определить JPEG формат для различных валидных JPEG данных")
    @ParameterizedTest
    @MethodSource("jpegDataProvider")
    void shouldDetectJpegForVariousValidData(byte[] jpegData) {
        MediaType result = contentTypeDetector.detect(jpegData);

        assertEquals(MediaType.IMAGE_JPEG, result);
    }

    @DisplayName("Должен определить PNG формат для различных валидных PNG данных")
    @ParameterizedTest
    @MethodSource("pngDataProvider")
    void shouldDetectPngForVariousValidData(byte[] pngData) {
        MediaType result = contentTypeDetector.detect(pngData);

        assertEquals(MediaType.IMAGE_PNG, result);
    }

    @DisplayName("Должен вернуть OCTET_STREAM для невалидных данных изображения")
    @ParameterizedTest
    @MethodSource("invalidImageDataProvider")
    void shouldReturnOctetStreamForInvalidImageData(byte[] invalidData) {
        MediaType result = contentTypeDetector.detect(invalidData);

        assertEquals(MediaType.APPLICATION_OCTET_STREAM, result);
    }

    @DisplayName("Должен выбросить исключение для null данных")
    @Test
    void shouldThrowExceptionForNullData() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> contentTypeDetector.detect(null));

        assertEquals("Image data cannot be null", exception.getMessage());
    }

    @DisplayName("Должен выбросить исключение для пустого массива данных")
    @Test
    void shouldThrowExceptionForEmptyData() {
        byte[] emptyData = new byte[0];

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> contentTypeDetector.detect(emptyData));

        assertEquals("Image data cannot be null", exception.getMessage());
    }

    @DisplayName("Должен корректно обрабатывать граничные случаи длины данных")
    @ParameterizedTest
    @MethodSource("edgeCasesDataProvider")
    void shouldHandleEdgeCasesDataLength(byte[] data) {
        MediaType result = contentTypeDetector.detect(data);

        assertTrue(result == MediaType.APPLICATION_OCTET_STREAM ||
                result == MediaType.IMAGE_JPEG ||
                result == MediaType.IMAGE_PNG);
    }

    @DisplayName("Должен отдавать приоритет JPEG при конфликте сигнатур")
    @Test
    void shouldPrioritizeJpegOverPng() {
        byte[] conflictingData = new byte[]{
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, // JPEG сигнатура
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A // PNG сигнатура
        };

        MediaType result = contentTypeDetector.detect(conflictingData);

        assertEquals(MediaType.IMAGE_JPEG, result);
    }
}
