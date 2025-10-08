//package io.github.habatoo.repository.impl.util;
//
//import io.github.habatoo.service.FileStorageService;
//import org.springframework.dao.DataAccessException;
//import org.springframework.dao.EmptyResultDataAccessException;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//
///**
// * Обработчик операций с изображениями постов.
// * Инкапсулирует логику работы с базой данных и файловым хранилищем.
// */
//public class ImageOperationHandler {
//
//    private final JdbcTemplate jdbcTemplate;
//    private final FileStorageService fileStorageService;
//
//    public ImageOperationHandler(JdbcTemplate jdbcTemplate, FileStorageService fileStorageService) {
//        this.jdbcTemplate = jdbcTemplate;
//        this.fileStorageService = fileStorageService;
//    }
//
//    /**
//     * Проверяет существование поста по идентификатору.
//     */
//    public void validatePostExists(Long postId) {
//        Integer postExists = jdbcTemplate.queryForObject(
//                "SELECT COUNT(*) FROM post WHERE id = ?",
//                Integer.class,
//                postId
//        );
//
//        if (postExists == null || postExists == 0) {
//            throw new EmptyResultDataAccessException("Post with id " + postId + " not found", 1);
//        }
//    }
//
//    /**
//     * Получает имя файла изображения для указанного поста.
//     */
//    public String getCurrentImageFileName(Long postId) {
//        try {
//            return jdbcTemplate.queryForObject(
//                    "SELECT image_url FROM post WHERE id = ?",
//                    String.class,
//                    postId
//            );
//        } catch (EmptyResultDataAccessException e) {
//            return null;
//        }
//    }
//
//    /**
//     * Обновляет метаданные изображения в базе данных.
//     */
//    public void updateImageInDatabase(Long postId, String originalFilename,
//                                      long size, String savedFileName) {
//        int updatedRows = jdbcTemplate.update(
//                "UPDATE post SET image_name = ?, image_size = ?, image_url = ?, updated_at = NOW() WHERE id = ?",
//                originalFilename,
//                size,
//                savedFileName,
//                postId
//        );
//
//        if (updatedRows == 0) {
//            throw new DataAccessException("Failed to update post image");
//        }
//    }
//
//    /**
//     * Обрабатывает обновление изображения поста.
//     */
//    public void handleImageUpdate(Long postId, MultipartFile image) throws IOException {
//        String oldFileName = getCurrentImageFileName(postId);
//        String savedFileName = fileStorageService.saveImageFile(postId, image);
//
//        updateImageInDatabase(postId, image.getOriginalFilename(), image.getSize(), savedFileName);
//
//        if (oldFileName != null) {
//            fileStorageService.deleteImageFile(oldFileName);
//        }
//    }
//
//    /**
//     * Обрабатывает загрузку изображения поста.
//     */
//    public byte[] handleImageRetrieval(Long postId) throws IOException {
//        String imageFileName = getCurrentImageFileName(postId);
//        if (imageFileName == null) {
//            throw new EmptyResultDataAccessException("Post with id " + postId + " has no image", 1);
//        }
//
//        return fileStorageService.loadImageFile(imageFileName);
//    }
//}
