package ch.bzz.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Utility class demonstrating proper exception handling.
 * 
 * Key concepts:
 * 1. Use ResponseStatusException for validation errors (reusable across layers)
 * 2. Always log exceptions before throwing
 * 3. Include original exception for debugging when appropriate
 * 4. Combine validation and extraction for efficiency
 * 
 * Note: This is a demonstration class. In production:
 * - Store JWT secret in environment variables or secure vault
 * - Use proper key rotation strategies
 * - Consider using Spring Security's built-in JWT support
 */
@Slf4j
@Component
public class JwtUtil {

    private final SecretKey key;

    /**
     * Initialize JWT utility with secret key from configuration.
     * Falls back to default key if not configured (development only).
     * 
     * @param secret JWT secret from application.properties (jwt.secret)
     */
    public JwtUtil(@Value("${jwt.secret:DefaultSecretKeyForDevelopmentOnlyMinimum32Characters}") String secret) {
        // Ensure secret is at least 32 characters for HMAC-SHA256
        if (secret.length() < 32) {
            log.warn("JWT secret is too short, using padded version");
            secret = secret + "0".repeat(32 - secret.length());
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        log.info("JwtUtil initialized");
    }

    /**
     * Generate a JWT token for a given subject (username/project name).
     * 
     * @param subject The subject (user/project identifier)
     * @return JWT token string
     */
    public String generateToken(String subject) {
        log.debug("Generating JWT token for subject: {}", subject);
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600000); // 1 hour
        
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * OLD APPROACH: Separate validation method (NOT RECOMMENDED)
     * 
     * Issues:
     * - Catches exception but doesn't log or rethrow it (swallows errors)
     * - Returns boolean requires separate call to extract subject
     * - Inefficient: parses token twice (once to validate, once to extract)
     * 
     * @param token JWT token to validate
     * @return true if valid, false otherwise
     * @deprecated Use verifyTokenAndExtractSubject() instead
     */
    @Deprecated
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // BAD PRACTICE: Exception is caught but ignored (not logged or rethrown)
            return false;
        }
    }

    /**
     * RECOMMENDED APPROACH: Verify token and extract subject in one call.
     * 
     * Benefits:
     * - Combines validation and extraction (more efficient)
     * - Uses ResponseStatusException (can be thrown from service layer)
     * - Properly logs exceptions
     * - Returns meaningful HTTP status (401 UNAUTHORIZED)
     * - Can be reused in multiple API methods
     * 
     * Example usage in controller:
     * <pre>
     * {@code
     * @GetMapping("/api/protected")
     * public String protectedEndpoint() {
     *     String projectName = jwtUtil.verifyTokenAndExtractSubject();
     *     return "Welcome, " + projectName;
     * }
     * }
     * </pre>
     * 
     * @return Subject extracted from valid JWT
     * @throws ResponseStatusException with 401 UNAUTHORIZED if token is invalid
     */
    public String verifyTokenAndExtractSubject() {
        try {
            String token = extractTokenFromHeader();
            return extractSubject(token);
        } catch (ResponseStatusException e) {
            // Re-throw ResponseStatusException as-is
            throw e;
        } catch (Exception e) {
            // Log the exception with details
            log.warn("Invalid token provided", e);
            
            // Option 1: Include original exception (good for debugging)
            // throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token", e);
            
            // Option 2: Don't include original exception (better security - less info leakage)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }

    /**
     * Extract subject from JWT token.
     * 
     * @param token JWT token string
     * @return Subject claim from token
     * @throws io.jsonwebtoken.JwtException if token is invalid
     */
    private String extractSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Extract JWT token from Authorization header.
     * Expected format: "Bearer <token>"
     * 
     * @return JWT token string (without "Bearer " prefix)
     * @throws ResponseStatusException if header is missing or malformed
     */
    private String extractTokenFromHeader() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes == null) {
            log.warn("No request attributes found (not in request context)");
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authorization header required"
            );
        }
        
        String authHeader = attributes.getRequest().getHeader("Authorization");
        
        if (authHeader == null || authHeader.trim().isEmpty()) {
            log.warn("Authorization header is missing");
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authorization header required"
            );
        }
        
        if (!authHeader.startsWith("Bearer ")) {
            log.warn("Authorization header does not start with 'Bearer '");
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid authorization header format. Expected: Bearer <token>"
            );
        }
        
        return authHeader.substring(7);
    }

    /**
     * Alternative method: Extract subject from explicit token string.
     * Useful when token comes from source other than Authorization header.
     * 
     * @param token JWT token string
     * @return Subject from token
     * @throws ResponseStatusException if token is invalid
     */
    public String extractSubjectFromToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Token cannot be null or empty"
                );
            }
            
            return extractSubject(token);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Failed to extract subject from token", e);
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid token",
                e
            );
        }
    }
}
