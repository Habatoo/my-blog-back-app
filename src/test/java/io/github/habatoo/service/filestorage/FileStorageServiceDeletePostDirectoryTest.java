package io.github.habatoo.service.filestorage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Тесты для метода deletePostDirectory.
 */
@DisplayName("Тесты метода deletePostDirectory")
class FileStorageServiceDeletePostDirectoryTest extends FileStorageServiceTestBase {

    @DisplayName("Должен удалить директорию поста со всеми файлами")
    @Test
    void shouldDeletePostDirectoryWithAllFiles() throws IOException {
        Path postDir = baseUploadPath.resolve(VALID_POST_ID.toString());
        Path file1 = postDir.resolve("image1.jpg");
        Path file2 = postDir.resolve("image2.png");
        Path subDir = postDir.resolve("nested");
        Path file3 = subDir.resolve("image3.gif");

        createTestFile(file1, "content1".getBytes());
        createTestFile(file2, "content2".getBytes());
        createTestFile(file3, "content3".getBytes());

        fileStorageService.deletePostDirectory(VALID_POST_ID);

        assertFalse(directoryExists(postDir));
        assertFalse(fileExists(file1));
        assertFalse(fileExists(file2));
        assertFalse(fileExists(file3));
    }

    @DisplayName("Должен удалять директории разных постов")
    @ParameterizedTest
    @MethodSource("postIdProvider")
    void shouldDeleteDifferentPostDirectories(Long postId) throws IOException {
        Path postDir = baseUploadPath.resolve(postId.toString());
        Path file = postDir.resolve("test.jpg");

        createTestFile(file, "content".getBytes());

        fileStorageService.deletePostDirectory(postId);

        assertFalse(directoryExists(postDir));
    }

    @DisplayName("Должен игнорировать удаление несуществующей директории")
    @Test
    void shouldIgnoreDeletingNonExistentDirectory() {
        assertDoesNotThrow(() -> fileStorageService.deletePostDirectory(999L));
    }

    @DisplayName("Должен удалять пустые директории")
    @Test
    void shouldDeleteEmptyDirectories() throws IOException {
        Path postDir = baseUploadPath.resolve(VALID_POST_ID.toString());
        Files.createDirectories(postDir);

        fileStorageService.deletePostDirectory(VALID_POST_ID);

        assertFalse(directoryExists(postDir));
    }
}
