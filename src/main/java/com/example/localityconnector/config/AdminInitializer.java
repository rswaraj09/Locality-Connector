package com.example.localityconnector.config;

import com.example.localityconnector.model.User;
import com.example.localityconnector.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Initializes or updates the default Admin user in MongoDB upon application startup
 * based on credentials supplied via environment variables (ADMIN_EMAIL and ADMIN_PASSWORD).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@locality-connector.in}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@123}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (adminEmail == null || adminEmail.isBlank()) {
            return;
        }

        String email = adminEmail.trim().toLowerCase();
        Optional<User> adminOpt = userRepository.findByEmail(email);

        if (adminOpt.isEmpty()) {
            User admin = new User();
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setName("Platform Administrator");
            admin.setRoles(List.of("ADMIN", "USER"));
            admin.setActive(true);
            admin.setEmailVerified(true);
            admin.prePersist();
            userRepository.save(admin);
            log.info("Initialized default Admin user in MongoDB: {}", email);
        } else {
            User admin = adminOpt.get();
            boolean updated = false;
            if (!passwordEncoder.matches(adminPassword, admin.getPassword())) {
                admin.setPassword(passwordEncoder.encode(adminPassword));
                updated = true;
            }
            if (admin.getRoles() == null || !admin.getRoles().contains("ADMIN")) {
                admin.setRoles(List.of("ADMIN", "USER"));
                updated = true;
            }
            if (!admin.isActive()) {
                admin.setActive(true);
                updated = true;
            }
            if (!admin.isEmailVerified()) {
                admin.setEmailVerified(true);
                updated = true;
            }
            if (updated) {
                admin.prePersist();
                userRepository.save(admin);
                log.info("Updated Admin user credentials & roles in MongoDB for: {}", email);
            }
        }
    }
}
