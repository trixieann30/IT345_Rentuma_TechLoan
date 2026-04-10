package edu.cit.rentuma.techloan.service;

import edu.cit.rentuma.techloan.dto.*;
import edu.cit.rentuma.techloan.model.User;
import edu.cit.rentuma.techloan.repository.UserRepository;
import edu.cit.rentuma.techloan.security.JwtUtil;
import edu.cit.rentuma.techloan.service.validator.RegistrationValidator;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Authentication service.
 *
 * Refactoring 1 – Builder Pattern (Creational):
 *   User objects are constructed through User.builder() which now includes
 *   a personalEmail() step, removing the need for reflection-based workarounds.
 *
 * Refactoring 2 – Strategy Pattern (Behavioral):
 *   Validation logic is no longer hardcoded inline. AuthService accepts a
 *   RegistrationValidator via constructor injection. Spring injects
 *   StandardRegistrationValidator by default; a different strategy (e.g.,
 *   GoogleRegistrationValidator) can be injected for OAuth flows.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RegistrationValidator validator;   // <-- Strategy

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       RegistrationValidator validator) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil         = jwtUtil;
        this.validator       = validator;
    }

    // ── REGISTER ─────────────────────────────────────────────────────

    public AuthResponse register(RegisterRequest request) {

        // Strategy: delegate all validation to the injected validator
        validator.validate(request);

        // Hash password
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Builder: construct User cleanly (no reflection)
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase())
                .passwordHash(hashedPassword)
                .studentId(request.getStudentId())
                .role(request.getRole())
                .penaltyPoints(0)
                .build();

        User savedUser = userRepository.save(user);

        String token        = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(savedUser.getEmail());

        return AuthResponse.builder()
                .success(true)
                .user(UserResponse.from(savedUser))
                .token(token)
                .refreshToken(refreshToken)
                .message("Account created successfully. Welcome to TechLoan!")
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // ── LOGIN ─────────────────────────────────────────────────────────

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() ->
                        new BadCredentialsException("AUTH-001:Invalid email or password"));

        if (user.getPasswordHash() == null ||
                !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("AUTH-001:Invalid email or password");
        }

        String token        = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .success(true)
                .user(UserResponse.from(user))
                .token(token)
                .refreshToken(refreshToken)
                .message("Welcome back, " + user.getFullName() + "!")
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // ── GET CURRENT USER ──────────────────────────────────────────────

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("DB-001:User not found"));
        return UserResponse.from(user);
    }
}