package ch.bzz.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS (Cross-Origin Resource Sharing) Configuration
 * 
 * CORS is a security mechanism that controls cross-origin HTTP requests.
 * Without CORS configuration, browsers block requests from different origins (domains/ports).
 * 
 * Why CORS is needed:
 * - Frontend hosted on GitHub Pages (https://username.github.io) needs to call your API
 * - Development: Frontend on localhost:3000 calling backend on localhost:8080
 * - Production: Frontend on different domain than backend
 * 
 * Security Note:
 * - CORS helps prevent CSRF (Cross-Site Request Forgery) attacks
 * - Only allow trusted origins in production
 * - Never use "*" (allow all origins) in production without authentication
 */
@Slf4j
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                log.info("Configuring CORS settings");
                
                registry.addMapping("/api/**") // Apply to all /api/* endpoints
                        // Allowed origins - adjust for your environment
                        .allowedOrigins(
                                "http://localhost:3000",           // React/Vue dev server
                                "http://localhost:4200",           // Angular dev server
                                "http://localhost:5173",           // Vite dev server
                                "http://localhost:8080",           // Local HTTP
                                "https://localhost:8443",          // Local HTTPS
                                "https://alexanderpeter.github.io" // GitHub Pages frontend
                        )
                        // Allowed HTTP methods
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        // Allowed headers
                        .allowedHeaders("*")
                        // Allow credentials (cookies, authorization headers)
                        .allowCredentials(true)
                        // How long browsers can cache preflight requests (in seconds)
                        .maxAge(3600);
                
                log.debug("CORS configured for /api/** endpoints");
            }
        };
    }
}
