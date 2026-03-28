package edu.cit.rentuma.techloan.controller;

import edu.cit.rentuma.techloan.dto.*;
import edu.cit.rentuma.techloan.service.AuthService;
import edu.cit.rentuma.techloan.service.GoogleAuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

    public AuthController(AuthService authService, GoogleAuthService googleAuthService) {
        this.authService = authService;
        this.googleAuthService = googleAuthService;
    }

    // ── POST /api/auth/register ──────────────────────────
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── POST /api/auth/login ─────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // ── POST /api/auth/google ────────────────────────────
    // Frontend sends Google ID token, we verify and authenticate
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleAuth(
            @Valid @RequestBody GoogleAuthRequest request) {
        AuthResponse response = googleAuthService.googleAuth(request);
        return ResponseEntity.ok(response);
    }

    // ── GET /api/auth/me ─────────────────────────────────
    // Requires: Authorization: Bearer <token>
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(user);
    }

    // ── DEBUG: Test password verification ─────────────────
    // This endpoint is for debugging only - remove in production
    @PostMapping("/debug/verify-password")
    public ResponseEntity<Map<String, Object>> debugVerifyPassword(
            @RequestBody LoginRequest request) {
        Map<String, Object> result = authService.debugVerifyPassword(request);
        return ResponseEntity.ok(result);
    }
}
