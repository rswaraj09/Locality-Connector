package com.example.localityconnector.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-path}")
    private String serviceAccountPath;

    @Value("${firebase.storage.bucket:}")
    private String storageBucket;

    private final ResourceLoader resourceLoader;

    public FirebaseConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    public Firestore firestore() {
        try {
            initializeFirebaseApp();
            return FirestoreClient.getFirestore();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not initialize Firebase. Verify 'firebase.service-account-path' (" + serviceAccountPath
                            + ") and the service-account credentials: " + e.getMessage(),
                    e);
        }
    }

    @Bean
    public Storage firebaseStorage() {
        try {
            initializeFirebaseApp();
            Resource resource = resourceLoader.getResource(serviceAccountPath);
            try (InputStream serviceAccount = resource.getInputStream()) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
                return StorageOptions.newBuilder()
                        .setCredentials(credentials)
                        .build()
                        .getService();
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not initialize Firebase Storage: " + e.getMessage(), e);
        }
    }

    private void initializeFirebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            Resource resource = resourceLoader.getResource(serviceAccountPath);
            try (InputStream serviceAccount = resource.getInputStream()) {
                FirebaseOptions.Builder builder = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount));
                if (storageBucket != null && !storageBucket.isBlank()) {
                    builder.setStorageBucket(storageBucket);
                }
                FirebaseApp.initializeApp(builder.build());
            }
        }
    }
}
