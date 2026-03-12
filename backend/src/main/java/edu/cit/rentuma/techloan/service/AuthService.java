package edu.cit.rentuma.techloan.service;

import edu.cit.rentuma.techloan.dto.*;
import edu.cit.rentuma.techloan.model.User;
import edu.cit.rentuma.techloan.repository.UserRepository;
import edu.cit.rentuma.techloan.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.institutional.domain}")
    private String institutionalDomain;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // ── REGISTER ─────────────────────────────────────────
    public AuthResponse register(RegisterRequest request) {

        // 1. Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("VALID-002:Passwords do not match");
        }

        // 2. Validate institutional email domain
        if (!request.getEmail().toLowerCase().endsWith("@" + institutionalDomain)) {
            throw new IllegalArgumentException(
                "VALID-003:Email must be a CIT-U institutional email (@" + institutionalDomain + ")");
        }

        // 3. Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("DB-002:An account with this email already exists");
        }

        // 4. Check for duplicate student ID
        if (request.getStudentId() != null &&
            userRepository.existsByStudentId(request.getStudentId())) {
            throw new IllegalStateException("DB-002:An account with this Student/Faculty ID already exists");
        }

        // 5. Hash password (bcrypt, strength 12)
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // 6. Build and save user
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase())
                .passwordHash(hashedPassword)
                .studentId(request.getStudentId())
                .role(request.getRole())
                .penaltyPoints(0)
                .build();

        User savedUser = userRepository.save(user);

        // 7. Generate tokens
        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());
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

    // ── LOGIN ────────────────────────────────────────────
    public AuthResponse login(LoginRequest request) {

        // 1. Find user by email
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() ->
                    new BadCredentialsException("AUTH-001:Invalid email or password"));

        // 2. Verify password against bcrypt hash
        if (user.getPasswordHash() == null ||
            !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("AUTH-001:Invalid email or password");
        }

        // 3. Generate tokens
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .success(true)
                .user(UserResponse.from(user))
                .token(token)
                .refreshToken(refreshToken)
                .message("Login successful")
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // ── GET CURRENT USER ─────────────────────────────────
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new RuntimeException("DB-001:User not found"));
        return UserResponse.from(user);
    }
}
