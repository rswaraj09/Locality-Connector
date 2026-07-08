package com.example.localityconnector.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

/**
 * Manages file uploads to Firebase/Google Cloud Storage. Validates file type
 * and size before upload.
 */
@Slf4j
@Service
public class StorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    private final Storage storage;
    private final String bucketName;

    public StorageService(
            Storage storage,
            @Value("${firebase.storage.bucket:}") String bucketName) {
        this.storage = storage;
        this.bucketName = bucketName;
    }

    /**
     * Upload an image file to Firebase Cloud Storage.
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
        String objectName = folder + "/" + UUID.randomUUID() + extension;

        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());

        // Return the Firebase Storage public URL
        String encodedName = URLEncoder.encode(objectName, StandardCharsets.UTF_8);
        String url = String.format(
                "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                bucketName, encodedName);

        log.info("Uploaded file to {}/{}", bucketName, objectName);
        return url;
    }

    /**
     * Delete a file from Firebase Cloud Storage by its URL.
     */
    public void deleteByUrl(String url) {
        if (url == null || url.isBlank()) return;
        try {
            // Extract object name from Firebase Storage URL
            String prefix = "/o/";
            int start = url.indexOf(prefix);
            if (start < 0) return;
            String encoded = url.substring(start + prefix.length());
            int queryIdx = encoded.indexOf('?');
            if (queryIdx > 0) encoded = encoded.substring(0, queryIdx);
            String objectName = java.net.URLDecoder.decode(encoded, StandardCharsets.UTF_8);

            BlobId blobId = BlobId.of(bucketName, objectName);
            boolean deleted = storage.delete(blobId);
            if (deleted) {
                log.info("Deleted file: {}", objectName);
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
