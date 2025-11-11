package ch.bzz.controller;

import ch.bzz.service.GreetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST Controller demonstrating:
 * 1. Dependency Injection using Constructor Injection
 * 2. Exception handling with ResponseStatusException
 * 3. Different approaches to error handling (ResponseEntity vs Exceptions)
 * 
 * Constructor Injection is the recommended approach in Spring Boot because:
 * - Fields can be final (immutable)
 * - Easier to test (can pass mocks in constructor)
 * - @Autowired is optional when there's only one constructor
 */
@Slf4j
@RestController
public class HelloController {

    // final field ensures immutability - only possible with constructor injection
    private final GreetingService greetingService;

    // Constructor Injection - @Autowired is optional with single constructor
    public HelloController(GreetingService greetingService) {
        this.greetingService = greetingService;
        log.info("HelloController initialized with GreetingService");
    }

    /**
     * Simple endpoint without validation.
     * 
     * @return Basic greeting
     */
    @GetMapping("/api/hello")
    public String hello() {
        log.debug("Hello endpoint called");
        return greetingService.getGreeting();
    }

    /**
     * Endpoint with path variable and exception handling.
     * Demonstrates how ResponseStatusException from service layer
     * automatically returns appropriate HTTP status codes.
     * 
     * Example requests:
     * - GET /api/hello/John     -> 200 OK: "Hello, John!"
     * - GET /api/hello/         -> 400 BAD_REQUEST: "Name cannot be null or empty"
     * - GET /api/hello/A        -> 400 BAD_REQUEST: "Name must be at least 2 characters long"
     * 
     * @param name The name to greet
     * @return Personalized greeting
     * @throws ResponseStatusException if name validation fails (400 BAD_REQUEST)
     */
    @GetMapping("/api/hello/{name}")
    public String helloName(@PathVariable String name) {
        log.debug("Hello endpoint called with name: {}", name);
        // Exception from service is automatically converted to HTTP response
        return greetingService.getCustomGreeting(name);
    }

    /**
     * Endpoint demonstrating validated greeting with proper exception handling.
     * 
     * Example requests:
     * - GET /api/greet?name=john       -> 200 OK: "Hello, John!"
     * - GET /api/greet?name=ALEXANDER  -> 200 OK: "Hello, Alexander!"
     * - GET /api/greet?name=           -> 400 BAD_REQUEST
     * - GET /api/greet                 -> 400 BAD_REQUEST
     * 
     * @param name The name to greet (query parameter)
     * @return Validated and formatted greeting
     * @throws ResponseStatusException if validation fails
     */
    @GetMapping("/api/greet")
    public String greetWithValidation(@RequestParam(required = false) String name) {
        log.debug("Greet endpoint called with name: {}", name);
        return greetingService.getValidatedGreeting(name);
    }

    /**
     * APPROACH 1: Using ResponseEntity (traditional approach)
     * 
     * Advantages:
     * - Explicit control over HTTP response
     * - Clear in the controller what status is returned
     * 
     * Disadvantages:
     * - Can only be used in controller methods (not in service layer)
     * - Verbose for simple cases
     * - Logic mixed with HTTP concerns
     * 
     * @param name The name to greet
     * @return ResponseEntity with greeting or error status
     */
    @GetMapping("/api/greet/response-entity/{name}")
    public ResponseEntity<String> greetWithResponseEntity(@PathVariable String name) {
        log.debug("ResponseEntity approach called with name: {}", name);
        
        // Manual validation and ResponseEntity creation
        if (name == null || name.trim().isEmpty()) {
            log.warn("Invalid name provided: null or empty");
            return ResponseEntity.badRequest().body("Name cannot be null or empty");
        }
        
        if (name.trim().length() < 2) {
            log.warn("Invalid name provided: too short");
            return ResponseEntity.badRequest().body("Name must be at least 2 characters long");
        }
        
        String greeting = greetingService.getCustomGreeting(name);
        return ResponseEntity.ok(greeting);
    }

    /**
     * APPROACH 2: Using ResponseStatusException (recommended)
     * 
     * Advantages:
     * - Can be thrown from service methods (reusable)
     * - Cleaner controller code
     * - Better separation of concerns
     * - Automatic HTTP status code handling
     * 
     * Disadvantages:
     * - Slightly less explicit in controller
     * - Exception handling can be less obvious
     * 
     * @param name The name to greet
     * @return Greeting string
     * @throws ResponseStatusException if validation fails
     */
    @GetMapping("/api/greet/exception/{name}")
    public String greetWithException(@PathVariable String name) {
        log.debug("Exception approach called with name: {}", name);
        
        // Validation logic can be in service layer
        // Exception automatically converted to HTTP response
        return greetingService.getCustomGreeting(name);
    }

    /**
     * Demonstrates exception handling with custom messages and logging.
     * 
     * Example showing how to catch, log, and re-throw exceptions
     * with additional context.
     * 
     * @param name The name to process
     * @return Processed greeting
     * @throws ResponseStatusException with logged context
     */
    @GetMapping("/api/greet/advanced/{name}")
    public String advancedGreeting(@PathVariable String name) {
        try {
            log.info("Advanced greeting requested for: {}", name);
            
            // Call service method that might throw exception
            String result = greetingService.getValidatedGreeting(name);
            
            log.info("Advanced greeting successfully generated for: {}", name);
            return result;
            
        } catch (ResponseStatusException e) {
            // Log the exception with context
            log.error("Failed to generate greeting for: {} - Status: {}, Reason: {}", 
                     name, e.getStatusCode(), e.getReason(), e);
            
            // Re-throw the exception (Spring handles HTTP response)
            throw e;
            
        } catch (Exception e) {
            // Catch unexpected exceptions and wrap them
            log.error("Unexpected error generating greeting for: {}", name, e);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                e // Include original exception for debugging
            );
        }
    }

    /**
     * Example endpoint demonstrating NOT_FOUND exception.
     * This pattern is commonly used when looking up resources.
     * 
     * Simulates a scenario where you might look up a user/project/resource.
     * 
     * @param id The ID to look up
     * @return Greeting for the found resource
     * @throws ResponseStatusException with 404 NOT_FOUND if ID is invalid
     */
    @GetMapping("/api/user/{id}/greeting")
    public String getUserGreeting(@PathVariable String id) {
        log.debug("Looking up user with ID: {}", id);
        
        // Simulate resource lookup (in real app, this would query database)
        if (!"123".equals(id) && !"456".equals(id)) {
            log.warn("User not found with ID: {}", id);
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User not found with ID: " + id
            );
        }
        
        log.debug("User found with ID: {}", id);
        return "Hello, User " + id + "!";
    }
}
