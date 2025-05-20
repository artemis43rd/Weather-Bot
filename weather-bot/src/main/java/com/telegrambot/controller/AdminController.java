package com.telegrambot.controller;

import com.telegrambot.model.User;
import com.telegrambot.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final AdminService adminService;

    @Value("${admin.username:admin}") // Default to "admin" if not set in properties
    private String expectedUsername;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.toLowerCase().startsWith("basic ")) {
            return challengeForCredentials("Authentication required.");
        }

        String base64Credentials = authorizationHeader.substring("Basic ".length()).trim();
        byte[] credDecoded;
        try {
            credDecoded = Base64.getDecoder().decode(base64Credentials);
        } catch (IllegalArgumentException e) {
            logger.warn("Error decoding Base64 credentials: {}", e.getMessage());
            return challengeForCredentials("Invalid credentials format.");
        }
        
        String credentials = new String(credDecoded, StandardCharsets.UTF_8);
        final String[] values = credentials.split(":", 2);

        if (values.length < 2) {
            return challengeForCredentials("Invalid credentials format.");
        }

        String username = values[0];
        String password = values[1];

        if (!expectedUsername.equals(username)) {
             return challengeForCredentials("Invalid username or password!");
        }

        // The try-catch block for SecurityException is not strictly necessary here
        // if @ExceptionHandler is present, as it will handle it.
        // However, it can be useful for catching other unexpected exceptions.
        try {
            List<User> users = adminService.getUsersIfAdmin(password);
            return ResponseEntity.ok(users);
        } catch (SecurityException ex) {
            // This block is now somewhat redundant if @ExceptionHandler(SecurityException.class) exists
            // If @ExceptionHandler is not present, this block would be triggered.
            logger.warn("Access attempt with invalid credentials (caught in method): {}", ex.getMessage());
            return challengeForCredentials(ex.getMessage()); // Use the message from the exception
        } catch (Exception ex) {
            // Generic handler for other unexpected errors
            logger.error("Unexpected error in getUsers: ", ex);
            Map<String, String> errorResponse = Map.of("error", "Internal Server Error", "message", "An internal server error occurred.");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<Map<String, String>> challengeForCredentials(String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"Admin Area\""); // Keep realm, it's standard
        Map<String, String> errorBody = Map.of(
            "error", "Authentication Required", // or "Authentication Failed"
            "message", message
        );
        return new ResponseEntity<>(errorBody, headers, HttpStatus.UNAUTHORIZED);
    }

    // Local handler for SecurityException thrown within this controller
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleSecurityException(SecurityException ex) {
        logger.warn("Access attempt with invalid credentials (handled by @ExceptionHandler): {}", ex.getMessage());
        // Create the JSON response body
        // Use the message from the exception, which you set in AdminService
        return challengeForCredentials(ex.getMessage());
    }
}