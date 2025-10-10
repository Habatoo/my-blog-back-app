package io.github.habatoo.service.imagecontenttypedetector;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тесты для приватного метода isJpeg
 */
@DisplayName("Тесты метода isJpeg")
class ImageContentTypeDetectorIsJpegTest extends ImageContentTypeDetectorTestBase {

    @DisplayName("Должен вернуть true для валидных JPEG сигнатур")
    @ParameterizedTest
    @MethodSource("jpegDataProvider")
    void shouldReturnTrueForValidJpegSignatures(byte[] jpegData) {
        boolean result = ReflectionTestUtils.invokeMethod(contentTypeDetector, "isJpeg", jpegData);

        assertTrue(result);
    }

    @DisplayName("Должен вернуть false для невалидных JPEG данных")
    @ParameterizedTest
    @MethodSource("invalidImageDataProvider")
    void shouldReturnFalseForInvalidJpegData(byte[] invalidData) {
        boolean result = ReflectionTestUtils.invokeMethod(contentTypeDetector, "isJpeg", invalidData);

        assertTrue(!result);
    }

    @DisplayName("Должен вернуть false для PNG данных")
    @ParameterizedTest
    @MethodSource("pngDataProvider")
    void shouldReturnFalseForPngData(byte[] pngData) {
        boolean result = ReflectionTestUtils.invokeMethod(contentTypeDetector, "isJpeg", pngData);

        assertTrue(!result);
    }

    @DisplayName("Должен вернуть false для данных недостаточной длины")
    @Test
    void shouldReturnFalseForInsufficientLength() {
        byte[] shortData = new byte[]{(byte) 0xFF, (byte) 0xD8};

        boolean result = ReflectionTestUtils.invokeMethod(contentTypeDetector, "isJpeg", shortData);

        assertTrue(!result);
    }

    @DisplayName("Должен вернуть false для данных с неверной сигнатурой")
    @Test
    void shouldReturnFalseForWrongSignature() {
        byte[] wrongSignature = new byte[]{(byte) 0xFE, (byte) 0xD8, (byte) 0xFF};

        boolean result = ReflectionTestUtils.invokeMethod(contentTypeDetector, "isJpeg", wrongSignature);

        assertTrue(!result);
    }
}
