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

        System.err.println("[AUTH_REGISTER_DEBUG] Registration attempt");
        System.err.println("[AUTH_REGISTER_DEBUG] Email: " + request.getEmail());
        System.err.println("[AUTH_REGISTER_DEBUG] Full Name: " + request.getFullName());
        System.err.println("[AUTH_REGISTER_DEBUG] Student ID: " + request.getStudentId());
        System.err.println("[AUTH_REGISTER_DEBUG] Role: " + request.getRole());
        System.err.println("[AUTH_REGISTER_DEBUG] Password length: " + (request.getPassword() != null ? request.getPassword().length() : "null"));
        System.err.println("[AUTH_REGISTER_DEBUG] Confirm Password length: " + (request.getConfirmPassword() != null ? request.getConfirmPassword().length() : "null"));

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
        System.err.println("[AUTH_REGISTER_DEBUG] Hashed password length: " + (hashedPassword != null ? hashedPassword.length() : "null"));

        // 6. Build and save user
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase())
                .passwordHash(hashedPassword)
                .studentId(request.getStudentId())
                .role(request.getRole())
                .penaltyPoints(0)
                .build();

        System.err.println("[AUTH_REGISTER_DEBUG] User built, password hash in user: " + (user.getPasswordHash() != null ? user.getPasswordHash().length() : "null"));

        User savedUser = userRepository.save(user);
        
        System.err.println("[AUTH_REGISTER_DEBUG] User saved, password hash in saved user: " + (savedUser.getPasswordHash() != null ? savedUser.getPasswordHash().length() : "null"));

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
        System.err.println("[AUTH_DEBUG] Login attempt for: " + request.getEmail());
        System.err.println("[AUTH_DEBUG] Password from request length: " + (request.getPassword() != null ? request.getPassword().length() : "null"));
        System.err.println("[AUTH_DEBUG] Password hash exists: " + (user.getPasswordHash() != null));
        
        if (user.getPasswordHash() == null ||
            !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            System.err.println("[AUTH_DEBUG] Password mismatch!");
            throw new BadCredentialsException("AUTH-001:Invalid email or password");
        }

        System.err.println("[AUTH_DEBUG] Password verified!");
        
        // 3. Generate tokens
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
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

    // ── GET CURRENT USER ─────────────────────────────────
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new RuntimeException("DB-001:User not found"));
        return UserResponse.from(user);
    }

    // ── DEBUG METHOD - Remove in production ───────────────
    public java.util.Map<String, Object> debugVerifyPassword(LoginRequest request) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        
        var userOpt = userRepository.findByEmail(request.getEmail().toLowerCase());
        
        if (userOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "User not found");
            return result;
        }
        
        User user = userOpt.get();
        result.put("user_found", true);
        result.put("email", user.getEmail());
        result.put("password_hash_exists", user.getPasswordHash() != null);
        result.put("password_input_length", request.getPassword().length());
        
        boolean matches = user.getPasswordHash() != null && 
                         passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        result.put("password_matches", matches);
        
        if (matches) {
            result.put("success", true);
            result.put("message", "Password verified successfully");
        } else {
            result.put("success", false);
            result.put("message", "Password verification failed");
        }
        
        return result;
    }
}
