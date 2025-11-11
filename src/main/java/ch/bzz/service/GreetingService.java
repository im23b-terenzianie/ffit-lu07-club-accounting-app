package ch.bzz.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service demonstrating proper exception handling with ResponseStatusException.
 * 
 * Benefits of ResponseStatusException:
 * - Can be thrown from service methods (not just controllers)
 * - Automatically returns appropriate HTTP status codes
 * - Can be reused across multiple API methods
 * - Better separation of concerns
 */
@Slf4j
@Service
public class GreetingService {

    public String getGreeting() {
        log.debug("Generating greeting message");
        return "Hello World!";
    }

    /**
     * Generates a custom greeting for a given name.
     * 
     * @param name The name to greet (must not be null or empty)
     * @return Personalized greeting
     * @throws ResponseStatusException with 400 BAD_REQUEST if name is invalid
     */
    public String getCustomGreeting(String name) {
        log.debug("Generating custom greeting for: {}", name);
        
        // Validation with ResponseStatusException
        // This can be thrown from nested methods and automatically returns proper HTTP status
        if (name == null || name.trim().isEmpty()) {
            log.warn("Invalid name provided: name is null or empty");
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Name cannot be null or empty"
            );
        }
        
        // Additional validation: reject offensive names
        if (name.trim().length() < 2) {
            log.warn("Invalid name provided: name too short ({})", name);
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Name must be at least 2 characters long"
            );
        }
        
        return "Hello, " + name.trim() + "!";
    }
    
    /**
     * Demonstrates exception handling in nested service methods.
     * ResponseStatusException can be thrown from any service layer method
     * and will automatically be converted to the appropriate HTTP response.
     * 
     * @param name The name to validate and process
     * @return Processed greeting message
     * @throws ResponseStatusException if validation fails
     */
    public String getValidatedGreeting(String name) {
        try {
            log.debug("Validating and generating greeting for: {}", name);
            
            // Call validation method (can throw exception)
            validateName(name);
            
            // Process the name
            String processedName = processName(name);
            
            return "Hello, " + processedName + "!";
            
        } catch (ResponseStatusException e) {
            // Re-throw ResponseStatusException as-is
            log.error("Validation failed for name: {}", name, e);
            throw e;
        } catch (Exception e) {
            // Wrap unexpected exceptions in ResponseStatusException
            log.error("Unexpected error processing greeting for name: {}", name, e);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error processing greeting",
                e // Include original exception for debugging
            );
        }
    }
    
    /**
     * Validates a name according to business rules.
     * This method can be reused across multiple service methods.
     * 
     * @param name The name to validate
     * @throws ResponseStatusException if validation fails
     */
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Name cannot be null or empty"
            );
        }
        
        if (name.trim().length() < 2) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Name must be at least 2 characters long"
            );
        }
        
        if (name.trim().length() > 50) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Name cannot exceed 50 characters"
            );
        }
    }
    
    /**
     * Processes a name (example: capitalize first letter).
     * 
     * @param name The name to process
     * @return Processed name
     */
    private String processName(String name) {
        String trimmed = name.trim();
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }
}
