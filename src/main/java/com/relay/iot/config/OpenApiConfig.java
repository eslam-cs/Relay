package com.relay.iot.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for JWT authentication.
 * 
 * This configuration adds the "Authorize" button to Swagger UI that allows users
 * to enter their JWT token once, and it will be automatically included in all
 * protected endpoint requests.
 * 
 * What this does:
 * 1. Defines a global security scheme called "bearerAuth" using HTTP Bearer tokens
 * 2. Adds the "Authorize" button to the top right of Swagger UI
 * 3. Marks all endpoints with @SecurityRequirement as protected (shows lock icon)
 * 4. Endpoints with @SecurityRequirements (empty) are marked as public (no lock)
 * 
 * How users interact with it:
 * - Click "Authorize" button in Swagger UI
 * - Paste JWT token (without "Bearer" prefix)
 * - Click "Authorize" to save
 * - Token is now automatically added to all protected endpoint requests
 */
@Configuration
public class OpenApiConfig {

    // Name of the security scheme - must match @SecurityRequirement(name = "bearerAuth") in controllers
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    /**
     * Configures OpenAPI documentation with JWT authentication support.
     * 
     * @return OpenAPI configuration with security scheme
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API metadata shown at the top of Swagger UI
                .info(new Info()
                        .title("IoT Data Processing API")
                        .version("0.1.0")
                        .description("IoT backend with JWT authentication. " +
                                "1) Call /auth/token to get a token " +
                                "2) Click 'Authorize' button (top right with lock icon) " +
                                "3) Paste token WITHOUT 'Bearer' prefix " +
                                "4) Click Authorize - now all protected endpoints work!"))
                
                // Apply security globally to all endpoints (unless @SecurityRequirements overrides it)
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                
                // Define the security scheme details
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)  // HTTP authentication
                                        .scheme("bearer")                 // Bearer token scheme
                                        .bearerFormat("JWT")              // Token format is JWT
                                        .description("Enter JWT token (without 'Bearer' prefix)")));
    }
}
