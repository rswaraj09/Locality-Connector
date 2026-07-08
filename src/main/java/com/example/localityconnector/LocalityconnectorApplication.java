package com.example.localityconnector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Application entry point.
 *
 * <p>Spring Security auto-configuration is intentionally NOT excluded: the application
 * relies on a fully configured {@code SecurityFilterChain} (stateless JWT). Scheduling is
 * enabled so the JWT blacklist can purge expired entries.</p>
 */
@SpringBootApplication
@EnableScheduling
@EnableCaching
public class LocalityconnectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(LocalityconnectorApplication.class, args);
	}

}
