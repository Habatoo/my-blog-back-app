package io.github.habatoo.service.filestorage;

import io.github.habatoo.service.FileStorageService;
import io.github.habatoo.service.impl.FileStorageServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Тесты для метода saveImageFile.
 */
class FileStorageServiceEdgeCasesTest extends FileStorageServiceTestBase {

    @DisplayName("Должен автоматически создавать upload директорию при включенной опции")
    @Test
    void shouldAutoCreateUploadDirectoryWhenEnabled() {
        Path customUploadDir = Paths.get("auto-create-test");

        FileStorageService service = new FileStorageServiceImpl(
                "auto-create-test",
                true,
                fileNameGenerator,
                pathResolver
        );

        assertTrue(directoryExists(customUploadDir));

        try {
            Files.walk(customUploadDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            // Ignore
                        }
                    });
        } catch (IOException e) {
            // Ignore cleanup
        }
    }

    @DisplayName("Должен обрабатывать очень длинные имена файлов")
    @Test
    void shouldHandleVeryLongFilenames() throws IOException {
        String longFilename = "a".repeat(100) + ".jpg";
        String generatedName = "12345_6789.jpg";
        byte[] content = "content".getBytes();
        MultipartFile file = createMockMultipartFile(longFilename, content);
        Path postDir = baseUploadPath.resolve(VALID_POST_ID.toString());
        Path filePath = postDir.resolve(generatedName);

        when(fileNameGenerator.generateFileName(longFilename)).thenReturn(generatedName);
        when(pathResolver.resolveFilePath(postDir, generatedName)).thenReturn(filePath);

        String result = fileStorageService.saveImageFile(VALID_POST_ID, file);

        assertNotNull(result);
        assertTrue(fileExists(filePath));
    }

    @DisplayName("Должен обрабатывать специальные символы в именах файлов")
    @Test
    void shouldHandleSpecialCharactersInFilenames() throws IOException {
        String specialFilename = "file with spaces and (special) chars.jpg";
        String generatedName = "12345_6789.jpg";
        byte[] content = "content".getBytes();
        MultipartFile file = createMockMultipartFile(specialFilename, content);
        Path postDir = baseUploadPath.resolve(VALID_POST_ID.toString());
        Path filePath = postDir.resolve(generatedName);

        when(fileNameGenerator.generateFileName(specialFilename)).thenReturn(generatedName);
        when(pathResolver.resolveFilePath(postDir, generatedName)).thenReturn(filePath);

        String result = fileStorageService.saveImageFile(VALID_POST_ID, file);

        assertNotNull(result);
        assertTrue(fileExists(filePath));
    }

    @DisplayName("Должен обрабатывать параллельные операции с файлами")
    @Test
    void shouldHandleParallelFileOperations() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Callable<String>> tasks = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            final int index = i;
            tasks.add(() -> {
                String filename = "image" + index + ".jpg";
                String generatedName = "12345_" + index + ".jpg";
                byte[] content = ("content" + index).getBytes();
                MultipartFile file = createMockMultipartFile(filename, content);
                Path postDir = baseUploadPath.resolve(VALID_POST_ID.toString());
                Path filePath = postDir.resolve(generatedName);

                when(fileNameGenerator.generateFileName(filename)).thenReturn(generatedName);
                when(pathResolver.resolveFilePath(postDir, generatedName)).thenReturn(filePath);

                return fileStorageService.saveImageFile(VALID_POST_ID, file);
            });
        }

        List<Future<String>> results = executor.invokeAll(tasks);
        executor.shutdown();

        for (Future<String> result : results) {
            assertNotNull(result.get());
        }

        assertTrue(directoryExists(baseUploadPath.resolve(VALID_POST_ID.toString())));
    }
}
