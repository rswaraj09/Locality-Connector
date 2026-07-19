package com.example.localityconnector.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configures Spring to serve uploaded files from the local filesystem
 * at the /uploads/** URL path.
 */
@Configuration
public class StorageConfig implements WebMvcConfigurer {

    @Value("${app.storage.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String resourceLocation = "file:" + uploadPath.toString().replace("\\", "/") + "/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation)
                .setCachePeriod(3600);
    }
}
