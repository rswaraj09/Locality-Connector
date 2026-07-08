package com.example.localityconnector.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Springdoc/OpenAPI configuration. Declares a global {@code bearerAuth} scheme
 * so the Swagger UI can send the JWT as a Bearer token.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI localityConnectorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Locality Connector API")
                        .description("REST API for discovering and managing local businesses, "
                                + "items, feedback and proximity search.")
                        .version("1.0.0")
                        .contact(new Contact().name("Locality Connector"))
                        .license(new License().name("MIT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Provide the JWT obtained from /api/auth/*/login")));
    }
}
