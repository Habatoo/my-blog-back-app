package io.github.habatoo.service.impl;

import io.github.habatoo.service.FileNameGenerator;
import io.github.habatoo.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Сервис для обработки файлов.
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final String uploadDir;
    private final FileNameGenerator fileNameGenerator;
    private final PathResolverImpl pathResolver;

    public FileStorageServiceImpl(
            @Value("${app.upload.dir:uploads/posts/}") String uploadDir,
            @Value("${app.upload.auto-create-dir:true}") boolean autoCreateDir,
            FileNameGenerator fileNameGenerator,
            PathResolverImpl pathResolver) {

        this.uploadDir = uploadDir;
        this.fileNameGenerator = fileNameGenerator;
        this.pathResolver = pathResolver;

        if (autoCreateDir) {
            createUploadDirectory();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String saveImageFile(Long postId, MultipartFile file) throws IOException {
        Path postDir = createPostDirectory(postId);
        String fileName = fileNameGenerator.generateFileName(file.getOriginalFilename());
        Path filePath = pathResolver.resolveFilePath(postDir, fileName);
        file.transferTo(filePath);
        return postId + "/" + fileName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] loadImageFile(String filename) throws IOException {
        Path filePath = pathResolver.resolveFilePath(filename);
        return Files.readAllBytes(filePath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteImageFile(String filename) throws IOException {
        Path filePath = pathResolver.resolveFilePath(filename);
        Files.deleteIfExists(filePath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePostDirectory(Long postId) {
        try {
            Path postDir = Paths.get(uploadDir, postId.toString()).normalize();

            if (!postDir.startsWith(Paths.get(uploadDir).normalize())) {
                throw new SecurityException("Attempt to access directory outside upload directory");
            }

            if (Files.exists(postDir) && Files.isDirectory(postDir)) {
                deleteDirectoryRecursively(postDir);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error deleting post directory: " + e.getMessage(), e);
        }
    }

    /**
     * Создает директорию для конкретного поста.
     *
     * @param postId идентификатор поста
     * @return путь к созданной директории
     * @throws IOException если не удалось создать директорию
     */
    private Path createPostDirectory(Long postId) throws IOException {
        Path postDir = Paths.get(uploadDir, postId.toString());
        if (!Files.exists(postDir)) {
            Files.createDirectories(postDir);
        }
        return postDir;
    }

    /**
     * Рекурсивно удаляет директорию со всем содержимым
     *
     * @param directory путь к директории для удаления
     * @throws IOException при ошибках удаления
     */
    private void deleteDirectoryRecursively(Path directory) throws IOException {
        if (Files.exists(directory) && Files.isDirectory(directory)) {
            try (Stream<Path> paths = Files.walk(directory)) {
                paths.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to delete: " + path, e);
                            }
                        });
            }
        }
    }

    /**
     * Создает директорию для загрузки файлов при старте приложения
     */
    private void createUploadDirectory() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create upload directory: " + uploadDir, e);
        }
    }
}
