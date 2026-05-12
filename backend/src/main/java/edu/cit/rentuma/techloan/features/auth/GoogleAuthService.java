package edu.cit.rentuma.techloan.features.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import edu.cit.rentuma.techloan.features.auth.dto.*;
import edu.cit.rentuma.techloan.features.auth.model.User;
import edu.cit.rentuma.techloan.features.auth.observer.AuthEventPublisher;
import edu.cit.rentuma.techloan.features.auth.repository.UserRepository;
import edu.cit.rentuma.techloan.shared.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthEventPublisher eventPublisher;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    public GoogleAuthService(UserRepository userRepository,
                             JwtUtil jwtUtil,
                             AuthEventPublisher eventPublisher) {
        this.userRepository  = userRepository;
        this.jwtUtil         = jwtUtil;
        this.eventPublisher  = eventPublisher;
    }

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

    public AuthResponse googleAuth(GoogleAuthRequest request) {
        try {
            GoogleIdToken.Payload payload = verifyIdToken(request.getIdToken());

            String googleId      = payload.getSubject();
            String email         = payload.getEmail();
            String fullName      = (String) payload.get("name");
            String role               = request.getRole();
            String institutionalEmail = request.getInstitutionalEmail();

            if (institutionalEmail != null) {
                institutionalEmail = institutionalEmail.trim().toLowerCase();
                if (institutionalEmail.isBlank()) {
                    institutionalEmail = null;
                }
            }

            try {
                User.Role.valueOf(role);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("VALID-005:Invalid role");
            }

            User user = userRepository.findByGoogleId(googleId).orElse(null);
            if (user == null) {
                if (institutionalEmail == null || !institutionalEmail.endsWith("@cit.edu")) {
                    throw new IllegalArgumentException("AUTH-005:Google registration requires a valid CIT-U email (@cit.edu)");
                }

                user = User.builder()
                        .fullName(fullName)
                        .email(email.toLowerCase())
                        .googleId(googleId)
                        .role(User.Role.valueOf(role))
                        .institutionalEmail(institutionalEmail)
                        .penaltyPoints(0)
                        .emailVerified(false)
                        .verificationToken(UUID.randomUUID().toString())
                        .build();
            } else {
                user.setFullName(fullName);
                user.setEmail(email.toLowerCase());
                if (institutionalEmail != null && !institutionalEmail.equals(user.getInstitutionalEmail())) {
                    if (!institutionalEmail.endsWith("@cit.edu")) {
                        throw new IllegalArgumentException("AUTH-005:Google registration requires a valid CIT-U email (@cit.edu)");
                    }
                    user.setInstitutionalEmail(institutionalEmail);
                    user.setEmailVerified(false);
                    user.setVerificationToken(UUID.randomUUID().toString());
                }
            }

            User savedUser = userRepository.save(user);

            eventPublisher.publishGoogleAuthSuccess(savedUser);

            // If email is not verified, don't return token; ask user to verify first
            if (!savedUser.isEmailVerified()) {
                return AuthResponse.builder()
                        .success(true)
                        .user(UserResponse.from(savedUser))
                        .token(null)
                        .refreshToken(null)
                        .message("Please verify your CIT-U email. Check your inbox for the verification link.")
                        .timestamp(LocalDateTime.now().toString())
                        .build();
            }

            String token        = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());
            String refreshToken = jwtUtil.generateRefreshToken(savedUser.getEmail());

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
