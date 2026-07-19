package com.example.localityconnector.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * Manages file uploads to the local filesystem. Validates file type
 * and size before upload. Files are served via Spring's static resource handler.
 */
@Slf4j
@Service
public class StorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    @Value("${app.storage.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not create upload directory: " + uploadDir, e);
        }
    }

    /**
     * Upload an image file to local storage.
     *
     * @param file   the uploaded multipart file
     * @param folder the storage folder (e.g., "logos", "items")
     * @return the public URL of the uploaded file
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : ".jpg";
        String filename = UUID.randomUUID() + extension;

        Path folderPath = Paths.get(uploadDir, folder);
        if (!Files.exists(folderPath)) {
            Files.createDirectories(folderPath);
        }

        Path targetPath = folderPath.resolve(filename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        String url = baseUrl + "/uploads/" + folder + "/" + filename;
        log.info("Uploaded file to {}", targetPath.toAbsolutePath());
        return url;
    }

    /**
     * Delete a file from local storage by its URL.
     */
    public void deleteByUrl(String url) {
        if (url == null || url.isBlank()) return;
        try {
            // Extract relative path from URL: /uploads/folder/filename
            String marker = "/uploads/";
            int start = url.indexOf(marker);
            if (start < 0) return;
            String relativePath = url.substring(start + marker.length());

            Path filePath = Paths.get(uploadDir, relativePath);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Deleted file: {}", filePath);
            }
        } catch (Exception e) {
            log.warn("Failed to delete file from storage: {}", e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum of 5 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Invalid file type. Allowed types: JPEG, PNG, WebP, GIF");
        }
    }
}
