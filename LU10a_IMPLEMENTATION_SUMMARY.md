# LU10a - Exception Handling Implementation Summary

## ✅ Implementation Complete

Your Spring Boot application now demonstrates comprehensive exception handling using `ResponseStatusException` and best practices for error handling in REST APIs.

---

## 📁 Files Created/Modified

### 1. **GreetingService.java** ✅ (Updated)
**Location**: `src/main/java/ch/bzz/service/GreetingService.java`

**Added features:**
- ✅ Input validation with `ResponseStatusException`
- ✅ Proper exception logging with `@Slf4j`
- ✅ Multiple validation methods (name length, null checks)
- ✅ Demonstrates reusable validation in service layer
- ✅ Example of nested exception handling

**Key methods:**
- `getCustomGreeting(name)` - Validates name and throws 400 BAD_REQUEST if invalid
- `getValidatedGreeting(name)` - Advanced validation with try-catch
- `validateName(name)` - Private reusable validation method

---

### 2. **HelloController.java** ✅ (Updated)
**Location**: `src/main/java/ch/bzz/controller/HelloController.java`

**Added features:**
- ✅ Multiple endpoints demonstrating different exception approaches
- ✅ Comparison of ResponseEntity vs ResponseStatusException
- ✅ Advanced error handling with logging
- ✅ Resource not found examples (404)

**New endpoints:**

| Endpoint | Purpose | Demo |
|----------|---------|------|
| `GET /api/hello/{name}` | Basic with validation | Path variable validation |
| `GET /api/greet?name=...` | Query param validation | Advanced validation |
| `GET /api/greet/response-entity/{name}` | ResponseEntity approach | Traditional method |
| `GET /api/greet/exception/{name}` | Exception approach | Recommended method |
| `GET /api/greet/advanced/{name}` | Advanced logging | Full error context |
| `GET /api/user/{id}/greeting` | Resource lookup | 404 NOT_FOUND demo |

---

### 3. **JwtUtil.java** ✅ (NEW)
**Location**: `src/main/java/ch/bzz/util/JwtUtil.java`

**Features:**
- ✅ JWT token generation
- ✅ Token validation with proper exception handling
- ✅ Combined validation + extraction (efficient, single parse)
- ✅ Demonstrates OLD vs NEW approach
- ✅ Proper logging of authentication failures
- ✅ Extracts token from Authorization header

**Key methods:**
- `generateToken(subject)` - Creates JWT for user/project
- `verifyTokenAndExtractSubject()` - ✅ Recommended: validates and extracts in one call
- `validateToken(token)` - ❌ Deprecated: old approach (don't use)
- `extractSubjectFromToken(token)` - Alternative for explicit token strings

---

### 4. **build.gradle** ✅ (Updated)
**Added JWT dependencies:**
```gradle
implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'
```

---

### 5. **EXCEPTION_HANDLING_GUIDE.md** ✅ (NEW)
**Comprehensive documentation covering:**
- ResponseStatusException usage
- Three approaches to error handling (comparison)
- JWT validation best practices
- Exception logging patterns
- Testing examples with cURL
- Best practices summary
- Common HTTP status codes

---

## 🎯 Key Concepts Implemented

### 1. ResponseStatusException (Recommended Approach)

**Before (Traditional):**
```java
Optional<Project> optProject = repository.findById(id);
if (optProject.isEmpty()) {
    return ResponseEntity.notFound().build();
}
Project project = optProject.get();
```

**After (Exception-Based):**
```java
Project project = repository.findById(id)
    .orElseThrow(() -> new ResponseStatusException(
        HttpStatus.NOT_FOUND,
        "Project not found"
    ));
```

**Benefits:**
- ✅ Reusable in service layer
- ✅ Cleaner code
- ✅ Automatic HTTP status
- ✅ Better separation of concerns

---

### 2. Proper Exception Logging

**❌ Bad (Swallows exception):**
```java
try {
    validateToken(token);
    return true;
} catch (Exception e) {
    return false;  // Exception lost!
}
```

**✅ Good (Logs and throws):**
```java
try {
    return extractSubject(token);
} catch (Exception e) {
    log.warn("Invalid token", e);
    throw new ResponseStatusException(
        HttpStatus.UNAUTHORIZED,
        "Invalid token"
    );
}
```

---

### 3. Combined Validation + Extraction

**❌ Inefficient (parses twice):**
```java
if (jwtUtil.validateToken(token)) {         // Parse #1
    String subject = jwtUtil.extract(token); // Parse #2
}
```

**✅ Efficient (parses once):**
```java
String subject = jwtUtil.verifyTokenAndExtractSubject(); // Parse once
```

---

## 🧪 Testing Examples

### Valid Requests (200 OK)

```powershell
# Basic greeting
curl -k https://localhost:8443/api/hello

# Custom greeting
curl -k https://localhost:8443/api/hello/John

# User greeting
curl -k https://localhost:8443/api/user/123/greeting
```

---

### Validation Errors (400 BAD_REQUEST)

```powershell
# Name too short
curl -k https://localhost:8443/api/hello/A

# Empty name
curl -k "https://localhost:8443/api/greet?name="

# ResponseEntity approach (for comparison)
curl -k https://localhost:8443/api/greet/response-entity/A
```

**Response:**
```json
{
  "timestamp": "2025-11-11T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Name must be at least 2 characters long",
  "path": "/api/hello/A"
}
```

---

### Not Found (404 NOT_FOUND)

```powershell
# User not found
curl -k https://localhost:8443/api/user/999/greeting
```

**Response:**
```json
{
  "timestamp": "2025-11-11T10:30:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with ID: 999",
  "path": "/api/user/999/greeting"
}
```

---

## 📊 HTTP Status Codes Used

| Code | Status | Usage Example |
|------|--------|---------------|
| 200 | OK | Successful request |
| 400 | BAD_REQUEST | Invalid input (empty name, too short, etc.) |
| 401 | UNAUTHORIZED | Invalid JWT token |
| 403 | FORBIDDEN | Valid auth but no permission |
| 404 | NOT_FOUND | Resource doesn't exist |
| 409 | CONFLICT | Resource already exists |
| 500 | INTERNAL_SERVER_ERROR | Unexpected error |

---

## 🎓 Learning Objectives Achieved

### ✅ LU10a Concepts Demonstrated

1. **Exception vs Checks**
   - Explained when to use exceptions (unexpected situations)
   - Explained when to avoid exceptions (performance-critical, happy path)

2. **ResponseStatusException Benefits**
   - Reusability across layers (service, controller, util)
   - Automatic HTTP status code handling
   - Better separation of concerns

3. **Exception Logging**
   - Never swallow exceptions
   - Always log before throwing
   - Include context in log messages
   - Option to include/exclude original exception

4. **JWT Validation**
   - ❌ Old approach: separate validation method (deprecated)
   - ✅ New approach: combined verify + extract
   - Proper exception handling in authentication

5. **Practical Examples**
   - Name validation in service layer
   - Resource lookup (404)
   - JWT token verification (401)
   - Multiple validation rules

---

## 🚀 How to Run & Test

### 1. Start the Application

```powershell
# Ensure JDK 21 is installed
winget install --id Eclipse.Adoptium.Temurin.21.JDK -e

# Run the application
.\gradlew.bat bootRun
```

### 2. Test Basic Endpoint

```powershell
curl -k https://localhost:8443/api/hello
```

**Expected:**
```
Hello World!
```

### 3. Test Validation

```powershell
# Valid name
curl -k https://localhost:8443/api/hello/John
# Output: Hello, John!

# Invalid name (too short)
curl -k https://localhost:8443/api/hello/A
# Output: 400 Bad Request - "Name must be at least 2 characters long"
```

### 4. Test Resource Lookup

```powershell
# Found
curl -k https://localhost:8443/api/user/123/greeting
# Output: Hello, User 123!

# Not found
curl -k https://localhost:8443/api/user/999/greeting
# Output: 404 Not Found - "User not found with ID: 999"
```

### 5. Compare Approaches

```powershell
# ResponseEntity approach
curl -k https://localhost:8443/api/greet/response-entity/John

# Exception approach (recommended)
curl -k https://localhost:8443/api/greet/exception/John

# Both return same result, but exception approach is more reusable
```

---

## 📋 Best Practices Checklist

Your application now follows these best practices:

- [x] Uses `ResponseStatusException` for error handling
- [x] Logs all exceptions before throwing
- [x] Combines validation and extraction (JWT example)
- [x] Throws exceptions from service layer (reusable)
- [x] Uses appropriate HTTP status codes
- [x] Provides meaningful error messages
- [x] Avoids swallowing exceptions
- [x] Separates concerns (HTTP vs business logic)
- [x] Demonstrates both old and new approaches
- [x] Includes comprehensive documentation

---

## 🔗 Related Documentation

- **Exception Guide**: `EXCEPTION_HANDLING_GUIDE.md` - Full tutorial with examples
- **Service Layer**: `src/main/java/ch/bzz/service/GreetingService.java`
- **Controller**: `src/main/java/ch/bzz/controller/HelloController.java`
- **JWT Util**: `src/main/java/ch/bzz/util/JwtUtil.java`

---

## 🎯 Quick Reference

### Throw Exception from Service

```java
@Service
public class MyService {
    public User getUser(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User not found"
            ));
    }
}
```

### Use in Controller

```java
@RestController
public class MyController {
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        // Exception automatically returns HTTP 404
        return myService.getUser(id);
    }
}
```

### Log and Throw

```java
try {
    return processRequest();
} catch (Exception e) {
    log.error("Failed to process", e);
    throw new ResponseStatusException(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Processing failed",
        e
    );
}
```

---

## 🎉 Success!

Your Spring Boot application now demonstrates:
- ✅ Professional exception handling
- ✅ ResponseStatusException best practices
- ✅ Proper logging patterns
- ✅ JWT validation with error handling
- ✅ Multiple error handling approaches
- ✅ Comprehensive testing endpoints

**Next Steps:**
1. Run the application: `.\gradlew.bat bootRun`
2. Test with cURL commands above
3. Review `EXCEPTION_HANDLING_GUIDE.md` for detailed explanations
4. Apply these patterns to your own controllers and services

**Happy coding! 🚀**
