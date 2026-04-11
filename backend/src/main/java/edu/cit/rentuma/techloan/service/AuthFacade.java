package edu.cit.rentuma.techloan.service;

import edu.cit.rentuma.techloan.dto.*;
import org.springframework.stereotype.Component;

/**
 * Refactoring 3 – Facade Pattern (Structural):
 *
 * AuthFacade provides a single, simplified entry point for all
 * authentication operations. It hides the fact that two separate
 * services (AuthService, GoogleAuthService) exist under the hood.
 *
 * Benefits:
 *  - AuthController depends on ONE object instead of two.
 *  - Adding a third auth method (e.g., SSO) only requires changes here,
 *    not in the controller.
 *  - The controller is reduced to a thin HTTP adapter.
 */
@Component
public class AuthFacade {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

    public AuthFacade(AuthService authService,
                      GoogleAuthService googleAuthService) {
        this.authService       = authService;
        this.googleAuthService = googleAuthService;
    }

    public AuthResponse register(RegisterRequest request) {
        return authService.register(request);
    }

    public AuthResponse login(LoginRequest request) {
        return authService.login(request);
    }

    public AuthResponse googleAuth(GoogleAuthRequest request) {
        return googleAuthService.googleAuth(request);
    }

    public UserResponse getCurrentUser(String email) {
        return authService.getCurrentUser(email);
    }
}