package com.example.localityconnector.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class MapTilerConfig {
    
    @Value("${maptiler.api.key}")
    private String mapTilerApiKey;
    
    @Bean
    public WebClient mapTilerWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.maptiler.com")
                .defaultHeader("Authorization", "Bearer " + mapTilerApiKey)
                .build();
    }
    
    @Bean
    public String mapTilerApiKey() {
        return mapTilerApiKey;
    }
}



