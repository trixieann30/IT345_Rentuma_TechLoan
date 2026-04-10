package edu.cit.rentuma.techloan.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import edu.cit.rentuma.techloan.dto.*;
import edu.cit.rentuma.techloan.model.User;
import edu.cit.rentuma.techloan.observer.AuthEventPublisher;
import edu.cit.rentuma.techloan.repository.UserRepository;
import edu.cit.rentuma.techloan.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;

/**
 * Google OAuth authentication service.
 *
 * Refactoring 1 – Builder Pattern (Creational):
 *   personalEmail is now set via User.builder().personalEmail(…) instead of
 *   the fragile reflection workaround that existed before.
 *
 * Refactoring 4 – Observer Pattern (Behavioral):
 *   After a successful Google auth, an event is published via
 *   AuthEventPublisher so interested observers (e.g., audit log, email
 *   service) can react without coupling this service to them.
 */
@Service
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthEventPublisher eventPublisher;   // <-- Observer

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    public GoogleAuthService(UserRepository userRepository,
                             JwtUtil jwtUtil,
                             AuthEventPublisher eventPublisher) {
        this.userRepository  = userRepository;
        this.jwtUtil         = jwtUtil;
        this.eventPublisher  = eventPublisher;
    }

    // ── Verify Google ID Token ──────────────────────────────────────

    public GoogleIdToken.Payload verifyIdToken(String idTokenString)
            throws GeneralSecurityException, IOException {

        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), jsonFactory)
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null) {
            throw new IllegalArgumentException("AUTH-004:Invalid Google ID token");
        }
        return idToken.getPayload();
    }

    // ── Google Register / Login ──────────────────────────────────────

    public AuthResponse googleAuth(GoogleAuthRequest request) {
        try {
            GoogleIdToken.Payload payload = verifyIdToken(request.getIdToken());

            String googleId     = payload.getSubject();
            String email        = payload.getEmail();
            String fullName     = (String) payload.get("name");
            String role         = request.getRole();
            String personalEmail = request.getPersonalEmail();

            // Validate role
            try {
                User.Role.valueOf(role);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("VALID-005:Invalid role");
            }

            // Find or create user
            // Builder fix: personalEmail now set via builder (no reflection)
            User user = userRepository.findByGoogleId(googleId)
                    .orElseGet(() -> User.builder()
                            .fullName(fullName)
                            .email(email.toLowerCase())
                            .googleId(googleId)
                            .role(User.Role.valueOf(role))
                            .personalEmail(personalEmail)   // <-- no reflection!
                            .penaltyPoints(0)
                            .build());

            // Update existing user fields
            if (user.getId() != null) {
                user.setFullName(fullName);
                user.setEmail(email.toLowerCase());
                user.setPersonalEmail(personalEmail);
            }

            User savedUser = userRepository.save(user);

            String token        = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());
            String refreshToken = jwtUtil.generateRefreshToken(savedUser.getEmail());

            // Observer: publish login event to any registered listeners
            eventPublisher.publishGoogleAuthSuccess(savedUser);

            return AuthResponse.builder()
                    .success(true)
                    .user(UserResponse.from(savedUser))
                    .token(token)
                    .refreshToken(refreshToken)
                    .message("Google authentication successful")
                    .timestamp(LocalDateTime.now().toString())
                    .build();

        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalArgumentException(
                    "AUTH-004:Failed to verify Google token: " + e.getMessage());
        }
    }
}