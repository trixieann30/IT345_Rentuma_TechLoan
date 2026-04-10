package edu.cit.rentuma.techloan.controller;

import edu.cit.rentuma.techloan.dto.*;
import edu.cit.rentuma.techloan.service.AuthFacade;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication REST controller.
 *
 * Refactoring 3 – Facade Pattern (Structural):
 *   AuthController now injects only AuthFacade. It no longer knows about
 *   AuthService or GoogleAuthService directly — it is purely a thin HTTP
 *   adapter that maps requests to facade calls and wraps responses with
 *   appropriate HTTP status codes.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthFacade authFacade;

    public AuthController(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    // ── POST /api/auth/register ───────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authFacade.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── POST /api/auth/login ──────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authFacade.login(request));
    }

    // ── POST /api/auth/google ─────────────────────────────────────────

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleAuth(
            @Valid @RequestBody GoogleAuthRequest request) {
        return ResponseEntity.ok(authFacade.googleAuth(request));
    }

    // ── GET /api/auth/me ──────────────────────────────────────────────

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authFacade.getCurrentUser(userDetails.getUsername()));
    }
}