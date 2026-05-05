package edu.cit.rentuma.techloan.features.auth;

import edu.cit.rentuma.techloan.features.auth.dto.*;
import org.springframework.stereotype.Component;

@Component
public class AuthFacade {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

    public AuthFacade(AuthService authService, GoogleAuthService googleAuthService) {
        this.authService       = authService;
        this.googleAuthService = googleAuthService;
    }

    public AuthResponse register(RegisterRequest request)     { return authService.register(request); }
    public AuthResponse login(LoginRequest request)           { return authService.login(request); }
    public AuthResponse googleAuth(GoogleAuthRequest request) { return googleAuthService.googleAuth(request); }
    public UserResponse getCurrentUser(String email)          { return authService.getCurrentUser(email); }
    public void logout(String email)                          { authService.logout(email); }
}
