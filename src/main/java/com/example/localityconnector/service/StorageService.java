package com.example.localityconnector.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Manages file uploads to Cloudinary CDN (when configured) or the local filesystem.
 * Validates file type and size before upload.
 */
@Slf4j
@Service
public class StorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    @Value("${app.storage.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${app.base-url:}")
    private String baseUrl;

    @Value("${cloudinary.cloud-name:${CLOUDINARY_CLOUD_NAME:}}")
    private String cloudinaryCloudName;

    @Value("${cloudinary.api-key:${CLOUDINARY_API_KEY:}}")
    private String cloudinaryApiKey;

    @Value("${cloudinary.api-secret:${CLOUDINARY_API_SECRET:}}")
    private String cloudinaryApiSecret;

    @Value("${cloudinary.url:${CLOUDINARY_URL:}}")
    private String cloudinaryUrl;

    private Cloudinary cloudinaryClient;

    @PostConstruct
    public void init() {
        initCloudinary();
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

    private void initCloudinary() {
        try {
            if (cloudinaryUrl != null && !cloudinaryUrl.isBlank()) {
                this.cloudinaryClient = new Cloudinary(cloudinaryUrl.trim());
                log.info("Cloudinary client initialized via CLOUDINARY_URL");
            } else if (cloudinaryCloudName != null && !cloudinaryCloudName.isBlank()
                    && cloudinaryApiKey != null && !cloudinaryApiKey.isBlank()
                    && cloudinaryApiSecret != null && !cloudinaryApiSecret.isBlank()) {
                this.cloudinaryClient = new Cloudinary(ObjectUtils.asMap(
                        "cloud_name", cloudinaryCloudName.trim(),
                        "api_key", cloudinaryApiKey.trim(),
                        "api_secret", cloudinaryApiSecret.trim(),
                        "secure", true
                ));
                log.info("Cloudinary client initialized for cloud: {}", cloudinaryCloudName.trim());
            } else {
                log.info("Cloudinary credentials not provided. Defaulting to local storage.");
            }
        } catch (Exception e) {
            log.warn("Failed to initialize Cloudinary client: {}", e.getMessage());
        }
    }

    /**
     * Upload an image file to Cloudinary (if configured) or local storage.
     *
     * @param file   the uploaded multipart file
     * @param folder the storage folder (e.g., "logos", "items")
     * @return the public URL of the uploaded file
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        validateFile(file);

        // 1. Try Cloudinary Upload if client is active
        if (cloudinaryClient != null) {
            try {
                Map<?, ?> uploadResult = cloudinaryClient.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                        "folder", "locality_connector/" + folder,
                        "resource_type", "image"
                ));
                String secureUrl = (String) uploadResult.get("secure_url");
                if (secureUrl != null && !secureUrl.isBlank()) {
                    log.info("Successfully uploaded image to Cloudinary: {}", secureUrl);
                    return secureUrl;
                }
            } catch (Exception e) {
                log.error("Cloudinary upload failed, falling back to local storage: {}", e.getMessage(), e);
            }
        }

        // 2. Fallback to Local Filesystem Storage
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : ".jpg";
        String filename = UUID.randomUUID() + extension;

        Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path folderPath = uploadRoot.resolve(folder).normalize();
        if (!folderPath.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Invalid storage folder");
        }
        if (!Files.exists(folderPath)) {
            Files.createDirectories(folderPath);
        }

        Path targetPath = folderPath.resolve(filename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        String relativePath = "/uploads/" + folder + "/" + filename;
        String finalUrl;
        if (baseUrl != null && !baseUrl.isBlank() && !baseUrl.contains("localhost")) {
            finalUrl = baseUrl.replaceAll("/+$", "") + relativePath;
        } else {
            finalUrl = relativePath;
        }
        log.info("Uploaded file locally to {} -> URL {}", targetPath.toAbsolutePath(), finalUrl);
        return finalUrl;
    }

    /**
     * Delete a file from Cloudinary or local storage by its URL.
     */
    public void deleteByUrl(String url) {
        if (url == null || url.isBlank()) return;
        try {
            if (url.contains("cloudinary.com") && cloudinaryClient != null) {
                deleteCloudinaryImage(url);
                return;
            }

            // Extract relative path from local URL: /uploads/folder/filename
            String marker = "/uploads/";
            int start = url.indexOf(marker);
            if (start < 0) return;
            String relativePath = url.substring(start + marker.length());

            Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadRoot.resolve(relativePath).normalize();

            if (!filePath.startsWith(uploadRoot)) {
                log.warn("Refused to delete path outside upload directory: {}", relativePath);
                return;
            }

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Deleted local file: {}", filePath);
            }
        } catch (Exception e) {
            log.warn("Failed to delete file from storage: {}", e.getMessage());
        }
    }

    private void deleteCloudinaryImage(String url) {
        try {
            // Cloudinary URL format: https://res.cloudinary.com/<cloud>/image/upload/v12345/locality_connector/logos/public_id.jpg
            int uploadIdx = url.indexOf("/upload/");
            if (uploadIdx > 0) {
                String afterUpload = url.substring(uploadIdx + 8);
                // Strip version prefix if present (v123456/)
                if (afterUpload.matches("^v\\d+/.*")) {
                    afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
                }
                // Strip file extension
                int dotIdx = afterUpload.lastIndexOf('.');
                String publicId = (dotIdx > 0) ? afterUpload.substring(0, dotIdx) : afterUpload;
                cloudinaryClient.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Deleted Cloudinary image with public_id: {}", publicId);
            }
        } catch (Exception e) {
            log.warn("Failed to delete Cloudinary image {}: {}", url, e.getMessage());
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
