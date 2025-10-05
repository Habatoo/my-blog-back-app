package io.github.habatoo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

@Service
public class FileStorageService {

    private final String uploadDir;

    public FileStorageService(@Value("${app.upload.dir:uploads/posts/}") String uploadDir,
                              @Value("${app.upload.auto-create-dir:true}") boolean autoCreateDir) {
        this.uploadDir = uploadDir;

        if (autoCreateDir) {
            createUploadDirectory();
        }
    }

    /**
     * Сохраняет файл изображения на диск
     *
     * @param postId идентификатор поста
     * @param file   файл изображения
     * @return относительный путь к сохраненному файлу
     * @throws IOException при ошибках сохранения файла
     */
    public String saveImageFile(Long postId, MultipartFile file) throws IOException {
        Path postUploadDir = Paths.get(uploadDir + postId + "/");
        if (!Files.exists(postUploadDir)) {
            Files.createDirectories(postUploadDir);
        }

        String fileExtension = getFileExtension(file.getOriginalFilename());
        String fileName = System.currentTimeMillis() + "." + fileExtension;
        Path filePath = postUploadDir.resolve(fileName);

        file.transferTo(filePath);

        return postId + "/" + fileName;
    }

    /**
     * Загружает файл изображения с диска
     *
     * @param filename относительный путь к файлу
     * @return ресурс с содержимым файла
     */
    public Resource loadImageFile(String filename) {
        try {
            Path filePath = getPath(filename);

            if (!Files.exists(filePath)) {
                throw new IllegalArgumentException("File not found: " + filename);
            }

            byte[] content = Files.readAllBytes(filePath);
            return new ByteArrayResource(content);
        } catch (IOException e) {
            throw new RuntimeException("Error reading image file: " + e.getMessage(), e);
        }
    }

    /**
     * Удаляет файл изображения с диска
     *
     * @param filename относительный путь к файлу
     */
    public void deleteImageFile(String filename) {
        try {
            Path filePath = getPath(filename);

            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error deleting image file: " + e.getMessage(), e);
        }
    }

    /**
     * Удаляет папку поста если она пустая
     *
     * @param postId идентификатор поста
     */
    public void deletePostDirectory(Long postId) {
        try {
            Path postDir = Paths.get(uploadDir + postId).normalize();

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
     * Проверяет тип загружаемого изображения
     *
     * @param image файл изображения
     * @return true если тип изображения поддерживается
     */
    public boolean isValidImageType(MultipartFile image) {
        String contentType = image.getContentType();
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif") ||
                        contentType.equals("image/jpg")
        );
    }

    /**
     * Извлекает путь до файла из имени
     *
     * @param filename имя файла
     * @return путь до файла
     */
    private Path getPath(String filename) {
        Path path = Paths.get(uploadDir);
        Path filePath = path.resolve(filename).normalize();

        if (!filePath.startsWith(path.normalize())) {
            throw new SecurityException("Attempt to access file outside upload directory");
        }
        return filePath;
    }


    /**
     * Извлекает расширение файла из имени
     *
     * @param filename имя файла
     * @return расширение файла
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
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
